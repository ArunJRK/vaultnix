package com.innowate.services.payment.routes

import io.micrometer.observation.annotation.Observed
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
class Health {

    private val logger = LoggerFactory.getLogger(Health::class.java)

    @GetMapping
    @Observed(name = "health.check", contextualName = "get-health")
    fun getHealth(): ResponseEntity<Map<String, String>> {
        logger.info("Health Route Pinged!!")
        val healthStatus = mapOf(
            "status" to "UP",
            "message" to "Service is healthy",
            "timestamp" to java.time.Instant.now().toString()
        )
        return ResponseEntity.ok(healthStatus)
    }
}