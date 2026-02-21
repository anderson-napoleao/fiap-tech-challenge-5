package br.com.condominio.servico.usuario.adapter.in.web.dto;

public record AtualizarUsuarioRequest(
    String nomeCompleto,
    String telefone,
    String cpf,
    String apartamento,
    String bloco
) {
}
