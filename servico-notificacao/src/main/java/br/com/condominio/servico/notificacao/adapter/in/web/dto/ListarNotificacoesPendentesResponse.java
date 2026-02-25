package br.com.condominio.servico.notificacao.adapter.in.web.dto;

import java.util.List;

/**
 * DTO usado para entrada e saida da API HTTP.
 */
public record ListarNotificacoesPendentesResponse(
    List<NotificacaoPendenteResponse> notificacoes,
    int page,
    int size
) {
}
