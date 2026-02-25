package br.com.condominio.servico.encomenda.infrastructure.web.error;

import java.time.OffsetDateTime;

/**
 * Padroniza tratamento e resposta de erros HTTP.
 */
public record ErrorResponse(
    OffsetDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {
}
