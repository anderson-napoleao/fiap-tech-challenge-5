package br.com.condominio.identidade.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.condominio.identidade.application.port.in.CriarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.in.DesabilitarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.in.RemoverUsuarioAdminUseCase;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class UseCaseContractTest {

  @Test
  void useCasesDevemReceberApenasCommandComoEntrada() {
    validarMetodoDeEntrada(CriarUsuarioAdminUseCase.class);
    validarMetodoDeEntrada(RemoverUsuarioAdminUseCase.class);
    validarMetodoDeEntrada(DesabilitarUsuarioAdminUseCase.class);
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
