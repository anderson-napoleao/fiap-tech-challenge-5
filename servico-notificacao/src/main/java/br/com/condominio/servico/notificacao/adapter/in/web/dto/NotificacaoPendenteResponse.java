package br.com.condominio.servico.notificacao.adapter.in.web.dto;

import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import java.time.Instant;

public record NotificacaoPendenteResponse(
    String id,
    String encomendaId,
    CanalNotificacao canal,
    String destino,
    String mensagem,
    StatusNotificacao status,
    Instant criadaEm,
    Instant enviadaEm
) {
}
