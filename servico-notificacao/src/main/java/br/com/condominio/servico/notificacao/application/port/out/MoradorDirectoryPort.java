package br.com.condominio.servico.notificacao.application.port.out;

import java.util.List;

/**
 * Define o contrato de saida da aplicacao para integracoes externas.
 */
public interface MoradorDirectoryPort {

  record Morador(
      String identityId,
      String nomeCompleto,
      String email
  ) {
    public Morador {
      validarObrigatorio(identityId, "identityId obrigatorio");
      validarObrigatorio(nomeCompleto, "nomeCompleto obrigatorio");
      validarObrigatorio(email, "email obrigatorio");
    }

    private static void validarObrigatorio(String valor, String mensagem) {
      if (valor == null || valor.isBlank()) {
        throw new IllegalArgumentException(mensagem);
      }
    }
  }

  List<Morador> listarMoradoresPorUnidade(String bloco, String apartamento);
}
