package br.com.condominio.servico.usuario.adapter.in.web.dto;

/**
 * DTO usado para entrada e saida da API HTTP.
 */
public record AtualizarUsuarioRequest(
    String nomeCompleto,
    String telefone,
    String cpf,
    String apartamento,
    String bloco
) {
}
