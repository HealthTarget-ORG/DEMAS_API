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
                    .description("""
                        ### Propósito Principal
                        O objetivo desta API é fornecer dados consolidados e de fácil acesso sobre a disponibilidade de medicamentos em unidades de saúde públicas do município de Boa Viagem - CE. Ela atua como uma camada intermediária que consome, processa e agrega dados públicos para otimizar a consulta e promover a transparência na saúde.

                        ### Contexto do Projeto
                        Este projeto foi desenvolvido como parte da disciplina de Projeto Integrador do curso de **Análise e Desenvolvimento de Sistemas do IFCE _campus_ Boa Viagem**.

                        ### Fonte de Dados e Avisos
                        - A principal fonte de dados é a **DEMAS - API de Dados Abertos do Ministério da Saúde** ([apidadosabertos.saude.gov.br](https://apidadosabertos.saude.gov.br)).
                        - Nossa API resolve as complexidades da fonte original (como a necessidade de paginação extensiva e a falta de agrupamentos), oferecendo endpoints rápidos e prontos para consumo.
                        - A atualização dos dados na fonte original pode levar, em média, até 17 dias. Nosso sistema é projetado para buscar e processar a versão mais recente dos dados disponíveis diariamente.
                        - Quaisquer discrepâncias nas informações, relativamente aos dados fornecidos pelo DEMAS, devem ser reportadas à equipe de desenvolvimento do projeto do governo federal.

                        ---

                        ### Equipe e Colaboradores
                        - **Douglas Holanda** - Desenvolvedor - [GitHub](https://github.com/Doug16Yanc/)
                        - **Kaiane Maciel** - Desenvolvedora - [GitHub](https://github.com/KaianeSousa/)
                        - **Letícia do Vale** - Desenvolvedora - [GitHub](https://github.com/Leititcia)
                        - **Wesley Rodrigues** - Desenvolvedor - [GitHub](https://github.com/Wesley00s/)
                    """.trimIndent())
                    .contact(
                        Contact()
                            .name("Contato Principal do Projeto")
                            .email("wesley300rodrigues@gmail.com")
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