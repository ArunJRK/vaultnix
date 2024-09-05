package com.innowate.services.payment.crons

// import org.springframework.beans.factory.annotation.Value
// import org.springframework.scheduling.annotation.Scheduled
// import org.springframework.stereotype.Component
// import org.springframework.web.reactive.function.client.WebClient

// @Component
// class OrderHealth(
//     private val webClient: WebClient
// ) {

//     @Value("\${order.service.url}")
//     private lateinit var paymentServiceUrl: String

//     @Scheduled(fixedRate = 15000)
//     fun checkOrderServiceHealth() {
//         webClient.get()
//             .uri("$paymentServiceUrl/health")
//             .retrieve()
//             .bodyToMono(String::class.java)
//             .subscribe(
//                 { response -> println("Order service health check: $response \n\n") },
//                 { error -> println("Order service health check failed: ${error.message}") }
//             )
//     }
// }
