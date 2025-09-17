package com.example.demas_api.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DemasDataItemDto(
    @param:JsonProperty("codigo_uf")
    val codigoUf: Int,

    val uf: String,

    @param:JsonProperty("codigo_municipio")
    val codigoMunicipio: Int,

    val municipio: String,

    @param:JsonProperty("codigo_cnes")
    val codigoCnes: String,

    @param:JsonProperty("nome_fantasia")
    val nomeFantasia: String,

    @param:JsonProperty("data_posicao_estoque")
    val dataPosicaoEstoque: String,

    @param:JsonProperty("codigo_catmat")
    val codigoCatmat: String,

    @param:JsonProperty("descricao_produto")
    val descricaoProduto: String,

    @param:JsonProperty("quantidade_estoque")
    val quantidadeEstoque: Int,

    val logradouro: String?,

    @param:JsonProperty("numero_endereco")
    val numeroEndereco: String?,

    val bairro: String?,

    val telefone: String?,

    val email: String?,

    val cep: String?,

    val latitude: Double?,

    val longitude: Double?
)