package br.com.condominio.servico.encomenda.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO usado para entrada e saida da API HTTP.
 */
public record ReceberEncomendaRequest(
    @NotBlank String nomeDestinatario,
    @NotBlank String apartamento,
    @NotBlank String bloco,
    @NotBlank String descricao
) {
}
