package com.innowate.services.payment

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.io.ClassPathResource
import org.springframework.scheduling.annotation.EnableScheduling

data class IngressConfig(
	val name: String,
	val hostname: String,
	val port: Int,
	val ingress: List<String>
)

@SpringBootApplication
@EnableScheduling
class PaymentApplication {

	private val logger = LoggerFactory.getLogger(PaymentApplication::class.java)

	private val ingressConfig: IngressConfig by lazy { loadIngressConfig() }

	private fun loadIngressConfig(): IngressConfig {
		logger.info("🏄‍♂️ Loading ingress config from ingress.json")
		val mapper = jacksonObjectMapper()
		val ingressJson = ClassPathResource("ingress.json").inputStream.readBytes().toString(Charsets.UTF_8)
		logger.debug("📜 Loaded JSON: $ingressJson")
		return mapper.readValue<IngressConfig>(ingressJson).also {
			logger.info("🎉 Successfully parsed ingress config")
		}
	}

	@PostConstruct
	fun logConfig() {
		logger.debug("🏄‍♂️🏄‍♂️🏄‍♂️ Loaded ingress config: $ingressConfig 🏄‍♂️🏄‍♂️🏄‍♂️")
	}
}

fun main(args: Array<String>) {
	runApplication<PaymentApplication>(*args)
}