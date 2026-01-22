package com.example.gateway_service

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.URI

@Component
class DebugLogFilter : GlobalFilter, Ordered {

    private val log = LoggerFactory.getLogger(DebugLogFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        // Kirib kelayotgan asl so'rov (Postmandan kelgan)
        val originalPath = exchange.request.uri.path

        return chain.filter(exchange).then(Mono.fromRunnable {
            // Gateway qayta ishlagandan keyingi haqiqiy manzil
            val routeUrl: URI? = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR)

            log.info("🔍 DETEKTIV: --------------------------------------------------")
            log.info("📥 Kiruvchi (Original): $originalPath")
            log.info("📤 Chiquvchi (Target)  : $routeUrl")
            log.info("🏁 Status Code         : ${exchange.response.statusCode}")
            log.info("---------------------------------------------------------------")
        })
    }

    override fun getOrder(): Int {
        // Eng oxirida ishlashi uchun past daraja beramiz
        return Ordered.LOWEST_PRECEDENCE
    }
}