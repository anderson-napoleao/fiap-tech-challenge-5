package br.com.condominio.identidade.adapter.in.web.dto;

import java.util.List;

public record UsuarioResponse(String username, List<String> roles) {}
