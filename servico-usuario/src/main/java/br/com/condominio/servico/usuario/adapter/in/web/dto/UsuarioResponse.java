package br.com.condominio.servico.usuario.adapter.in.web.dto;

import br.com.condominio.servico.usuario.domain.TipoUsuario;

/**
 * DTO usado para entrada e saida da API HTTP.
 */
public record UsuarioResponse(
    Long id,
    String identityId,
    String nomeCompleto,
    String email,
    TipoUsuario tipo,
    String telefone,
    String cpf,
    String apartamento,
    String bloco
) {
}
