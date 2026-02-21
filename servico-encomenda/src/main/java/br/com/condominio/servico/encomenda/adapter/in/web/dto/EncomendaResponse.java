package br.com.condominio.servico.encomenda.adapter.in.web.dto;

import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import java.time.Instant;

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
