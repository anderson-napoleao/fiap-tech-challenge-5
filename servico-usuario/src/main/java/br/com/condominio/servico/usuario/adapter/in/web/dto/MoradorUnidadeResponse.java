package br.com.condominio.servico.usuario.adapter.in.web.dto;

/**
 * DTO de resposta para APIs HTTP.
 */
public record MoradorUnidadeResponse(
    String identityId,
    String nomeCompleto,
    String email
) {
}
