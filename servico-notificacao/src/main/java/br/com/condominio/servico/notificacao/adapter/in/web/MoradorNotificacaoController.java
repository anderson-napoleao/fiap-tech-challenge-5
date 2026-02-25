package br.com.condominio.servico.notificacao.adapter.in.web;

import br.com.condominio.servico.notificacao.adapter.in.web.dto.ConfirmacaoNotificacaoResponse;
import br.com.condominio.servico.notificacao.adapter.in.web.dto.ListarNotificacoesPendentesResponse;
import br.com.condominio.servico.notificacao.adapter.in.web.dto.NotificacaoPendenteResponse;
import br.com.condominio.servico.notificacao.application.port.in.ConfirmarRecebimentoNotificacaoUseCase;
import br.com.condominio.servico.notificacao.application.port.in.ListarNotificacoesPendentesUseCase;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/morador/notificacoes")
public class MoradorNotificacaoController {

  private static final Logger log = LoggerFactory.getLogger(MoradorNotificacaoController.class);

  private final ListarNotificacoesPendentesUseCase listarNotificacoesPendentesUseCase;
  private final ConfirmarRecebimentoNotificacaoUseCase confirmarRecebimentoNotificacaoUseCase;

  public MoradorNotificacaoController(
      ListarNotificacoesPendentesUseCase listarNotificacoesPendentesUseCase,
      ConfirmarRecebimentoNotificacaoUseCase confirmarRecebimentoNotificacaoUseCase
  ) {
    this.listarNotificacoesPendentesUseCase = listarNotificacoesPendentesUseCase;
    this.confirmarRecebimentoNotificacaoUseCase = confirmarRecebimentoNotificacaoUseCase;
  }

  @GetMapping
  public ListarNotificacoesPendentesResponse listarPendentes(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(name = "confirmada", defaultValue = "false") boolean confirmada,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    String moradorId = jwt == null ? null : jwt.getSubject();
    log.info("notification list requested by morador {}", resumirId(moradorId));

    ListarNotificacoesPendentesUseCase.Result result = listarNotificacoesPendentesUseCase.executar(
        new ListarNotificacoesPendentesUseCase.Command(moradorId, confirmada, page, size)
    );

    List<NotificacaoPendenteResponse> notificacoes = result.notificacoes().stream()
        .map(item -> new NotificacaoPendenteResponse(
            item.id(),
            item.encomendaId(),
            item.canal(),
            item.destino(),
            item.mensagem(),
            item.status(),
            item.criadaEm(),
            item.enviadaEm()
        ))
        .toList();

    return new ListarNotificacoesPendentesResponse(notificacoes, result.page(), result.size());
  }

  @PostMapping("/{id}/confirmacao")
  public ConfirmacaoNotificacaoResponse confirmar(
      @PathVariable("id") String notificacaoId,
      @AuthenticationPrincipal Jwt jwt
  ) {
    String moradorId = jwt == null ? null : jwt.getSubject();
    log.info(
        "notification confirmation requested for id {} by morador {}",
        resumirId(notificacaoId),
        resumirId(moradorId)
    );

    ConfirmarRecebimentoNotificacaoUseCase.Result result = confirmarRecebimentoNotificacaoUseCase.executar(
        new ConfirmarRecebimentoNotificacaoUseCase.Command(notificacaoId, moradorId)
    );

    return new ConfirmacaoNotificacaoResponse(
        result.id(),
        result.status(),
        result.confirmadaEm()
    );
  }

  private String resumirId(String value) {
    if (value == null || value.isBlank()) {
      return "unknown";
    }
    if (value.length() <= 6) {
      return value;
    }
    return "***" + value.substring(value.length() - 6);
  }
}
