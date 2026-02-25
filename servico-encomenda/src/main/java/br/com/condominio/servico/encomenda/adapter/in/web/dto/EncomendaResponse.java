package br.com.condominio.servico.encomenda.adapter.in.web.dto;

import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import java.time.Instant;

/**
 * DTO usado para entrada e saida da API HTTP.
 */
public record EncomendaResponse(
    Long id,
    String nomeDestinatario,
    String apartamento,
    String bloco,
    String descricao,
    String recebidoPor,
    StatusEncomenda status,
    Instant dataRecebimento
) {
}
