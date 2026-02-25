package br.com.condominio.servico.notificacao.adapter.in.messaging;

/**
 * DTO de entrada para consumo de eventos de mensageria.
 */
public record EncomendaRecebidaMessage(
    String eventId,
    int eventVersion,
    String occurredAt,
    long encomendaId,
    String nomeDestinatario,
    String apartamento,
    String bloco,
    String descricao,
    String recebidoPor,
    String status
) {
}
