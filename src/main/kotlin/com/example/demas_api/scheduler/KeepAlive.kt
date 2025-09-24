package com.example.demas_api.scheduler

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class KeepAlive (
    webClientBuilder: WebClient.Builder,

    @param:Value("\${health-check.url}")
    private val healthCheckUrl: String
) {
    private val logger = LoggerFactory.getLogger(KeepAlive::class.java)

    private val webClient = webClientBuilder.build()

    @Scheduled(fixedRate = 100000, initialDelay = 100000)
    fun keepAlive() {
        val response = webClient.get()
            .uri(healthCheckUrl)
            .retrieve()
            .toBodilessEntity()
            .block()

        logger.info("Keep alive status: ${response?.statusCode}")
    }

}