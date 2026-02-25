package br.com.condominio.servico.usuario.adapter.in.web.dto;

import br.com.condominio.servico.usuario.domain.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO usado para entrada e saida da API HTTP.
 */
public record CriarUsuarioRequest(
    @NotBlank String nomeCompleto,
    @NotBlank @Email String email,
    @NotBlank String senha,
    @NotNull TipoUsuario tipo,
    String telefone,
    String cpf,
    String apartamento,
    String bloco
) {
}
