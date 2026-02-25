package br.com.condominio.servico.notificacao.application.port.in;

import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import java.time.Instant;
import java.util.List;

/**
 * Define o contrato de entrada (use case) da aplicacao.
 */
public interface ListarNotificacoesPendentesUseCase {

  record Command(
      String moradorId,
      boolean confirmada,
      int page,
      int size
  ) {
    public Command {
      validarObrigatorio(moradorId, "Morador obrigatorio");
      if (confirmada) {
        throw new IllegalArgumentException("Somente filtro confirmada=false e suportado");
      }
      if (page < 0) {
        throw new IllegalArgumentException("Page invalida");
      }
      if (size <= 0 || size > 100) {
        throw new IllegalArgumentException("Size invalido");
      }
    }

    private static void validarObrigatorio(String valor, String mensagem) {
      if (valor == null || valor.isBlank()) {
        throw new IllegalArgumentException(mensagem);
      }
    }
  }

  record Item(
      String id,
      String encomendaId,
      CanalNotificacao canal,
      String destino,
      String mensagem,
      StatusNotificacao status,
      Instant criadaEm,
      Instant enviadaEm
  ) {
  }

  record Result(
      List<Item> notificacoes,
      int page,
      int size
  ) {
  }

  Result executar(Command command);
}
