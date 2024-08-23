package com.innowate.services.payment.config


import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.firewall.StrictHttpFirewall
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
class DynamicHostnamePredicate {
    private val lock = ReentrantReadWriteLock()
    private val allowedHostnames: MutableSet<String> = mutableSetOf()

    init {
        allowedHostnames.add("localhost:8443")
        allowedHostnames.add("localhost:6443")
        allowedHostnames.add("localhost")
        println("allowedHostnames:::::::::::  $allowedHostnames")
    }

    // This method now directly checks the current set of allowed hostnames
    fun isAllowed(hostname: String): Boolean {
        lock.read {
            return allowedHostnames.contains(hostname)
        }
    }

    fun addAllowedHostname(hostname: String) {
        lock.write {
            allowedHostnames.add(hostname)
        }
    }

    fun removeAllowedHostname(hostname: String) {
        lock.write {
            allowedHostnames.remove(hostname)
        }
    }

    fun getAllowedHostnames(): Set<String> {
        lock.read {
            return allowedHostnames.toSet()
        }
    }
}

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val dynamicHostnamePredicate: DynamicHostnamePredicate
) {

    @Bean
    fun httpFirewall(): StrictHttpFirewall {
        val firewall = StrictHttpFirewall()

        // Use the dynamic predicate for allowed hostnames
        firewall.setAllowedHostnames { hostname ->
            dynamicHostnamePredicate.isAllowed(hostname)
        }

        // Allow all HTTP methods
        firewall.setAllowSemicolon(true)
        firewall.setAllowBackSlash(true)
        firewall.setAllowUrlEncodedPercent(true)
        firewall.setAllowUrlEncodedPeriod(true)
        firewall.setAllowUrlEncodedSlash(true)
        firewall.setAllowUrlEncodedDoubleSlash(true)
        firewall.setAllowedHttpMethods(listOf("GET", "POST"))

        return firewall
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf ->
                csrf.disable()  // Disable CSRF protection
            }
            .authorizeHttpRequests { authorize ->
                authorize.anyRequest().permitAll()
            }
        return http.build()
    }
}



@RestController
@RequestMapping("/hosts")
class HostController(
    private val dynamicHostnamePredicate: DynamicHostnamePredicate
) {
    private val logger = LoggerFactory.getLogger(HostController::class.java)

    @PostMapping("/add")
    fun addHost(@RequestParam host: String) {
        dynamicHostnamePredicate.addAllowedHostname(host)
        logger.info("Added $host to allowed hosts. Current allowed hosts: ${dynamicHostnamePredicate.getAllowedHostnames()}")
    }

    @PostMapping("/remove")
    fun removeHost(@RequestParam host: String) {
        dynamicHostnamePredicate.removeAllowedHostname(host)
        logger.info("Removed $host from allowed hosts. Current allowed hosts: ${dynamicHostnamePredicate.getAllowedHostnames()}")
    }

    @PostMapping("/set")
    fun setHosts(@RequestBody hosts: Set<String>) {
        hosts.forEach { dynamicHostnamePredicate.addAllowedHostname(it) }
        logger.info("Set allowed hosts to: $hosts")
    }

    @GetMapping("/all")
    fun getHosts(): Set<String> {
        return dynamicHostnamePredicate.getAllowedHostnames()
    }
}