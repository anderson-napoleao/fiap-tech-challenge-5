package br.com.condominio.servico.notificacao.adapter.in.web.dto;

import java.util.List;

public record ListarNotificacoesPendentesResponse(
    List<NotificacaoPendenteResponse> notificacoes,
    int page,
    int size
) {
}
