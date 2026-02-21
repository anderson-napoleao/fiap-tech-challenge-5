package br.com.condominio.identidade.adapter.in.web.dto;

import java.util.List;

public record CriarUsuarioRequest(String username, String password, List<String> roles) {}
