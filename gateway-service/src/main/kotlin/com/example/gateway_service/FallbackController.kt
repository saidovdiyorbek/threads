package com.example.gateway_service

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/fallback")
class FallbackController{

    @GetMapping("/course")
    fun courseFallbackService(): ResponseEntity<Map<String, Any>>{
        val response = mapOf(
            "code" to 503,
            "message" to "Sorry, the course service is temporarily unavailable",
            "status" to "SERVICE_DOWN",
        )
        return ResponseEntity.status(503).body(response)
    }
}