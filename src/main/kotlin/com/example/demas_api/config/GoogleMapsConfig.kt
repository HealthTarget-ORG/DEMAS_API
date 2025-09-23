package com.example.demas_api.config

import com.google.maps.GeoApiContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GoogleMapsConfig {

    @Value("\${google.maps.api-key}")
    private lateinit var apiKey: String

    @Bean
    fun geoApiContext(): GeoApiContext {
        return GeoApiContext.Builder()
            .apiKey(apiKey)
            .build()
    }
}