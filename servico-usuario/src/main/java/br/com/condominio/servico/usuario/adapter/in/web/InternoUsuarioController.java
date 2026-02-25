package br.com.condominio.servico.usuario.adapter.in.web;

import br.com.condominio.servico.usuario.adapter.in.web.dto.MoradorUnidadeResponse;
import br.com.condominio.servico.usuario.application.port.in.ListarMoradoresPorUnidadeUseCase;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador HTTP de entrada que delega para casos de uso.
 */
@RestController
@RequestMapping("/interno/usuarios")
public class InternoUsuarioController {

  private static final Logger log = LoggerFactory.getLogger(InternoUsuarioController.class);

  private final ListarMoradoresPorUnidadeUseCase listarMoradoresPorUnidadeUseCase;

  public InternoUsuarioController(ListarMoradoresPorUnidadeUseCase listarMoradoresPorUnidadeUseCase) {
    this.listarMoradoresPorUnidadeUseCase = listarMoradoresPorUnidadeUseCase;
  }

  @GetMapping("/moradores")
  public List<MoradorUnidadeResponse> listarMoradoresPorUnidade(
      @RequestParam("bloco") String bloco,
      @RequestParam("apartamento") String apartamento
  ) {
    log.info("internal resident lookup requested for apartment {}-{}", bloco, apartamento);

    ListarMoradoresPorUnidadeUseCase.Result result = listarMoradoresPorUnidadeUseCase.executar(
        new ListarMoradoresPorUnidadeUseCase.Command(bloco, apartamento)
    );

    return result.moradores().stream()
        .map(item -> new MoradorUnidadeResponse(item.identityId(), item.nomeCompleto(), item.email()))
        .toList();
  }
}
