package com.innowate.services.order.crons

import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.annotation.Observed
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class PaymentHealth(
    private val webClient: WebClient,
    private val observationRegistry: ObservationRegistry
) {
    private val logger = LoggerFactory.getLogger(PaymentHealth::class.java)

    @Value("\${payment.service.url}")
    private lateinit var paymentServiceUrl: String

    @Scheduled(fixedRate = 15000)
    @Observed(name = "payment.health.check", contextualName = "payment-health-check")
    fun checkPaymentServiceHealth() {
        webClient.get()
            .uri("$paymentServiceUrl/health")
            .retrieve()
            .bodyToMono(String::class.java)
            .doOnSuccess { response ->
                logger.info("Payment service health check: $response")
                observationRegistry.getCurrentObservation()?.highCardinalityKeyValue("health.status", "success")
            }
            .doOnError { error ->
                logger.error("Payment service health check failed: ${error.message}")
                observationRegistry.getCurrentObservation()?.let { observation ->
                    observation.highCardinalityKeyValue("health.status", "failed")
                    observation.error(error)
                }
            }
            .subscribe()
    }
}