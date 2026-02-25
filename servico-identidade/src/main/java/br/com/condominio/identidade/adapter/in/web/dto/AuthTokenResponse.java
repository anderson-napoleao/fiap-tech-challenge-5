package br.com.condominio.identidade.adapter.in.web.dto;

/**
 * DTO usado para entrada e saida da API HTTP.
 */
public record AuthTokenResponse(String access_token, String token_type, long expires_in) {
}
