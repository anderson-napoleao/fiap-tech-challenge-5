package br.com.condominio.servico.encomenda.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO usado para entrada e saida da API HTTP.
 */
public record BaixarEncomendaRetiradaRequest(
    @NotBlank String retiradoPorNome
) {
}
