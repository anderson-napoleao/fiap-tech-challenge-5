package br.com.condominio.identidade.adapter.in.web.dto;

public record AuthTokenResponse(String access_token, String token_type, long expires_in) {
}
