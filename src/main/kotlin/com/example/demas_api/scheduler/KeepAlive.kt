package com.example.demas_api.scheduler

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class KeepAlive (
    webClientBuilder: WebClient.Builder
) {
    private val logger = LoggerFactory.getLogger(KeepAlive::class.java)

    private val webClient = webClientBuilder.build()

    @Scheduled(fixedRate = 3600)
    fun keepAlive() {
        val response = webClient.get()
            .uri("https://demas-api.onrender.com/health")
            .retrieve()
            .toBodilessEntity()
            .block()

        logger.info("Keep alive status: ${response?.statusCode}")
    }

}