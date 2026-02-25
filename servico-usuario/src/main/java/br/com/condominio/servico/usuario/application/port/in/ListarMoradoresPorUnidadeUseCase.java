package br.com.condominio.servico.usuario.application.port.in;

import java.util.List;

/**
 * Define o contrato de entrada (use case) da aplicacao.
 */
public interface ListarMoradoresPorUnidadeUseCase {

  record Command(
      String bloco,
      String apartamento
  ) {
    public Command {
      validarObrigatorio(bloco, "Bloco obrigatorio");
      validarObrigatorio(apartamento, "Apartamento obrigatorio");
    }

    private static void validarObrigatorio(String valor, String mensagem) {
      if (valor == null || valor.isBlank()) {
        throw new IllegalArgumentException(mensagem);
      }
    }
  }

  record Item(
      String identityId,
      String nomeCompleto,
      String email
  ) {
  }

  record Result(List<Item> moradores) {
  }

  Result executar(Command command);
}
