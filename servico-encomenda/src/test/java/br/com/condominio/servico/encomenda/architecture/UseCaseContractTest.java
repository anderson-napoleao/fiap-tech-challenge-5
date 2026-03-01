package br.com.condominio.servico.encomenda.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import br.com.condominio.servico.encomenda.application.port.in.BaixarEncomendaRetiradaUseCase;
import br.com.condominio.servico.encomenda.application.port.in.ListarEncomendasPortariaUseCase;
import br.com.condominio.servico.encomenda.application.port.in.ReceberEncomendaUseCase;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class UseCaseContractTest {

  @Test
  void useCasesDevemReceberApenasCommandComoEntrada() {
    validarMetodoDeEntrada(BaixarEncomendaRetiradaUseCase.class);
    validarMetodoDeEntrada(ListarEncomendasPortariaUseCase.class);
    validarMetodoDeEntrada(ReceberEncomendaUseCase.class);
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
