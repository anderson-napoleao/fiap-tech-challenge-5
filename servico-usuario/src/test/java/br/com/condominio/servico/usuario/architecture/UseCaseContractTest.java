package br.com.condominio.servico.usuario.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.condominio.servico.usuario.application.port.in.AtualizarMeuPerfilUseCase;
import br.com.condominio.servico.usuario.application.port.in.CadastrarUsuarioUseCase;
import br.com.condominio.servico.usuario.application.port.in.ListarMoradoresPorUnidadeUseCase;
import br.com.condominio.servico.usuario.application.port.in.ObterMeuPerfilUseCase;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class UseCaseContractTest {

  @Test
  void useCasesDevemReceberApenasCommandComoEntrada() {
    validarMetodoDeEntrada(CadastrarUsuarioUseCase.class);
    validarMetodoDeEntrada(AtualizarMeuPerfilUseCase.class);
    validarMetodoDeEntrada(ObterMeuPerfilUseCase.class);
    validarMetodoDeEntrada(ListarMoradoresPorUnidadeUseCase.class);
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
