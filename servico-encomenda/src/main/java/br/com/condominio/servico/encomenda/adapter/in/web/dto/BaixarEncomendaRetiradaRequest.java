package br.com.condominio.servico.encomenda.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record BaixarEncomendaRetiradaRequest(
    @NotBlank String retiradoPorNome
) {
}
