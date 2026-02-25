package br.com.condominio.identidade.adapter.in.web.dto;

/**
 * DTO usado para entrada e saida da API HTTP.
 */
public record AuthTokenRequest(String username, String password) {
}
