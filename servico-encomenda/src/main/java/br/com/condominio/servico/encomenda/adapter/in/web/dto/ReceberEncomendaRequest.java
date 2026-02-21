package br.com.condominio.servico.encomenda.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ReceberEncomendaRequest(
    @NotBlank String nomeDestinatario,
    @NotBlank String apartamento,
    @NotBlank String bloco,
    @NotBlank String descricao
) {
}
