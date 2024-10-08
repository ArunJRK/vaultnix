package com.innowate.services.payment.routes

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/health")
class Health {

    @GetMapping
    fun getHealth(): ResponseEntity<Map<String, String>> {
        val healthStatus = mapOf(
            "status" to "UP",
            "message" to "Service is healthy",
            "timestamp" to java.time.Instant.now().toString()
        )
        return ResponseEntity.ok(healthStatus)
    }

}