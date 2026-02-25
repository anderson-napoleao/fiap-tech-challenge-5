package br.com.condominio.servico.notificacao.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.condominio.servico.notificacao.application.port.in.ConfirmarRecebimentoNotificacaoUseCase;
import br.com.condominio.servico.notificacao.application.port.in.ListarNotificacoesPendentesUseCase;
import br.com.condominio.servico.notificacao.application.port.in.ProcessarEncomendaRecebidaUseCase;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class UseCaseContractTest {

  @Test
  void useCasesDevemReceberApenasCommandComoEntrada() {
    validarMetodoDeEntrada(ProcessarEncomendaRecebidaUseCase.class);
    validarMetodoDeEntrada(ConfirmarRecebimentoNotificacaoUseCase.class);
    validarMetodoDeEntrada(ListarNotificacoesPendentesUseCase.class);
  }

  private void validarMetodoDeEntrada(Class<?> useCaseInterface) {
    for (Method method : useCaseInterface.getDeclaredMethods()) {
      assertEquals(1, method.getParameterCount(), useCaseInterface.getSimpleName() + "." + method.getName());
      assertEquals(
          "Command",
          method.getParameterTypes()[0].getSimpleName(),
          useCaseInterface.getSimpleName() + "." + method.getName()
      );
    }
  }
}
