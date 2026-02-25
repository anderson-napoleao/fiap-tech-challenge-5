package br.com.condominio.servico.notificacao.adapter.in.web.dto;

import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import java.time.Instant;

public record ConfirmacaoNotificacaoResponse(
    String id,
    StatusNotificacao status,
    Instant confirmadaEm
) {
}
