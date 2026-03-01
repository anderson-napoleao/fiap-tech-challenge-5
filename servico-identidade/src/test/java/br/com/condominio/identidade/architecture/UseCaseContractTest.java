package br.com.condominio.identidade.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import br.com.condominio.identidade.application.port.in.CriarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.in.DesabilitarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.in.GerarTokenUseCase;
import br.com.condominio.identidade.application.port.in.RemoverUsuarioAdminUseCase;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class UseCaseContractTest {

  @Test
  void useCasesDevemReceberApenasCommandComoEntrada() {
    validarMetodoDeEntrada(CriarUsuarioAdminUseCase.class);
    validarMetodoDeEntrada(RemoverUsuarioAdminUseCase.class);
    validarMetodoDeEntrada(DesabilitarUsuarioAdminUseCase.class);
    validarMetodoDeEntrada(GerarTokenUseCase.class);
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
