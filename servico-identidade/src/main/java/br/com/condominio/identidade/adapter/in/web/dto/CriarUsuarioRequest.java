package br.com.condominio.identidade.adapter.in.web.dto;

public record CriarUsuarioRequest(String email, String password, String role) {
}
