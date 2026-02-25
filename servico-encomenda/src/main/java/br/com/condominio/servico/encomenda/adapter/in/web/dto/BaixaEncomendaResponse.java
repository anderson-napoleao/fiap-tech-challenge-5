package br.com.condominio.servico.encomenda.adapter.in.web.dto;

import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import java.time.Instant;

public record BaixaEncomendaResponse(
    Long id,
    StatusEncomenda status,
    Instant dataRetirada,
    String retiradoPorNome
) {
}
