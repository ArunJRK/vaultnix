package com.innowate.services.order.crons

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class PaymentHealth(
    private val webClient: WebClient
) {

    @Value("\${payment.service.url}")
    private lateinit var paymentServiceUrl: String

    @Scheduled(fixedRate = 15000)
    fun checkPaymentServiceHealth() {
        webClient.get()
            .uri("$paymentServiceUrl/health")
            .retrieve()
            .bodyToMono(String::class.java)
            .subscribe(
                { response -> println("Payment service health check: $response \n\n") },
                { error -> println("Payment service health check failed: ${error.message}") }
            )
    }
}
