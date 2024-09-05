package com.innowate.services.order.routes

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/ping")
class Ping {

    @GetMapping
    fun getPing(): ResponseEntity<Map<String, String>> {
        val healthStatus = mapOf(
            "status" to "UP",
            "message" to "Pong",
            "timestamp" to java.time.Instant.now().toString()
        )
        return ResponseEntity.ok(healthStatus)
    }

}