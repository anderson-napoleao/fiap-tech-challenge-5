package br.com.condominio.identidade.adapter.in.web.dto;

/**
 * DTO usado para entrada e saida da API HTTP.
 */
public record CriarUsuarioRequest(String email, String password, String role) {
}
