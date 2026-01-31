package com.example.gateway_service

import com.fasterxml.jackson.databind.ObjectMapper
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import kotlin.collections.set
import kotlin.jvm.java
import kotlin.run

@Component
class RequestBodyRewrite : RewriteFunction<String?, String> {
    override fun apply(exchange: ServerWebExchange, body: String?): Publisher<String> {
        exchange.attributes[CACHED_URL_KEY] = exchange.request.uri
        exchange.attributes[CACHED_REQUEST_BODY_KEY] = body ?: return Mono.empty()
        return body.run { Mono.just(this) }
    }
}


interface AuthService {
    fun getUserInfo(token: String): Mono<Map<String, Any?>>
}


@Service
class AuthServiceImpl(
    @Value("\${spring.security.oauth2.resourceserver.user-info-uri}") private val userInfoUri: String,
    private val webClientBuilder: WebClient.Builder,
    private val objectMapper: ObjectMapper,
) : AuthService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun getUserInfo(token: String): Mono<Map<String, Any?>> {
        return webClientBuilder.build()
            .get()
            .uri(userInfoUri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .bodyToMono<String>()
            .map { objectMapper.parseMap(it) }
            .doOnError { emptyMap<String, Any?>() }
    }
}