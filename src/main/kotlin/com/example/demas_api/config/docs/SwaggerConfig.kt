package com.example.demas_api.config.docs

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("API de Dados de Saúde - Consulta de Medicamentos")
                    .version("1.0.0")
                    .description(
                        "O propósito desta API é fornecer dados consolidados e de fácil acesso sobre a " +
                                "disponibilidade de medicamentos em unidades de saúde públicas. Ela atua como uma " +
                                "camada intermediária, consumindo, processando e agregando dados públicos para " +
                                "otimizar a consulta e promover a transparência.\n\n" +
                                "A principal fonte de dados é a **DEMAS - API de Dados Abertos do Ministério da Saúde** " +
                                "(https://apidadosabertos.saude.gov.br). Esta API resolve as complexidades da fonte " +
                                "original, como a necessidade de paginação extensiva e a falta de agrupamentos, " +
                                "oferecendo endpoints rápidos e prontos para o consumo por aplicações cliente."
                    )
                    .contact(
                        Contact()
                            .email("wesley300rodrigues@gmail.com")
                            .name("Wesley Rodrigues")
                            .url("https://github.com/Wesley00s/")
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
    }
}