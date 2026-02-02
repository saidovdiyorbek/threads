package com.example.gateway_service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.ReactiveAuthorizationManager

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.AuthorizationContext
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Mono
import kotlin.apply
import kotlin.collections.all
import kotlin.collections.set
import kotlin.jvm.java

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        jwtAuthenticationConverter: JwtAuthenticationConverter
    ): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.apply { } }
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .oauth2ResourceServer { server ->
                server.jwt {
                    it.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
                server.authenticationEntryPoint { exchange, ex ->
                    println("Auth error: ${ex.message}")
                    ex.printStackTrace()
                    return@authenticationEntryPoint Mono.fromRunnable { ex
                        exchange.response.statusCode = org.springframework.http.HttpStatus.UNAUTHORIZED
                    }
                }
            }
            .authorizeExchange {
                it
                    .pathMatchers("/actuator/**").permitAll()
                    .pathMatchers("/api/v1/auth/user/register").permitAll()
                    .pathMatchers("/api/v1/auth/oauth2/**").permitAll()
                    .pathMatchers("/error").permitAll()
                    .pathMatchers("/api/v1/posts/**").permitAll()
                    .anyExchange().access(monoPathManager())
            }.build()
    }


    @Suppress("ReactorTransformationOnMonoVoid")
    @Bean
    fun contextHeaderWebFilter(objectMapper: ObjectMapper): WebFilter {
        return WebFilter { exchange, chain ->
            return@WebFilter ReactiveSecurityContextHolder.getContext()
                .map { it.authentication }
                .flatMap { auth ->
                    val details = auth.details
                    if (details is Map<*, *> && details.keys.all { it is String }) {
                        val mutatedRequest = exchange.request.mutate()
                        val detailsJson = objectMapper.writeValueAsString(details).compress()
                        mutatedRequest.header(USER_DETAILS_HEADER_KEY, detailsJson)
                        exchange.attributes[USER_ID_HEADER_KEY] = details[USER_ID_KEY]
                        exchange.attributes[USER_NAME_HEADER_KEY] = details[USER_USERNAME_KEY]
                        val newExchange = exchange.mutate().request(mutatedRequest.build()).build()
                        return@flatMap chain.filter(newExchange)
                    } else {
                        return@flatMap chain.filter(exchange)
                    }
                }.switchIfEmpty(chain.filter(exchange))
        }
    }


    private fun monoPathManager() = ReactiveAuthorizationManager<AuthorizationContext> { mono, context ->
        mono.map { auth ->
            try {
                val requestPath = context.exchange.request.path.value()
                val serviceName = requestPath.extractServiceName()
                val details = auth.details
                return@map AuthorizationDecision(true)

            } catch (e: Exception) {
                e.printStackTrace()
                return@map AuthorizationDecision(false)
            }
        }
    }
}