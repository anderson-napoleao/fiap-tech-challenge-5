package br.com.condominio.servico.notificacao.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import br.com.condominio.servico.notificacao.application.port.in.ConfirmarRecebimentoNotificacaoUseCase;
import br.com.condominio.servico.notificacao.application.port.in.ListarNotificacoesPendentesUseCase;
import br.com.condominio.servico.notificacao.application.port.in.ProcessarEncomendaRecebidaPorUnidadeUseCase;
import br.com.condominio.servico.notificacao.application.port.in.ProcessarEncomendaRecebidaUseCase;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class UseCaseContractTest {

  @Test
  void useCasesDevemReceberApenasCommandComoEntrada() {
    validarMetodoDeEntrada(ProcessarEncomendaRecebidaUseCase.class);
    validarMetodoDeEntrada(ProcessarEncomendaRecebidaPorUnidadeUseCase.class);
    validarMetodoDeEntrada(ConfirmarRecebimentoNotificacaoUseCase.class);
    validarMetodoDeEntrada(ListarNotificacoesPendentesUseCase.class);
  }

  private void validarMetodoDeEntrada(Class<?> useCaseInterface) {
    for (Method method : useCaseInterface.getDeclaredMethods()) {
      Class<?> commandType = method.getParameterTypes()[0];
      assertEquals(1, method.getParameterCount(), useCaseInterface.getSimpleName() + "." + method.getName());
      assertEquals(
          "Command",
          commandType.getSimpleName(),
          useCaseInterface.getSimpleName() + "." + method.getName()
      );
      assertSame(
          useCaseInterface,
          commandType.getEnclosingClass(),
          useCaseInterface.getSimpleName() + "." + method.getName()
      );
    }
  }
}
