package com.innowate.services.order.config

import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class WebClientConfig {

    @Bean
    fun webClient(webClientBuilder: WebClient.Builder, ssl: WebClientSsl): WebClient {
        val sslContext = SslContextBuilder
            .forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build()

        val httpClient = HttpClient.create()
            .secure { spec -> spec.sslContext(sslContext) }

        return webClientBuilder
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .apply(ssl.fromBundle("order"))
            .build()
    }
}