package br.com.condominio.servico.encomenda.adapter.in.web;

import br.com.condominio.servico.encomenda.adapter.in.web.dto.BaixaEncomendaResponse;
import br.com.condominio.servico.encomenda.adapter.in.web.dto.BaixarEncomendaRetiradaRequest;
import br.com.condominio.servico.encomenda.adapter.in.web.dto.EncomendaResponse;
import br.com.condominio.servico.encomenda.adapter.in.web.dto.ReceberEncomendaRequest;
import br.com.condominio.servico.encomenda.application.port.in.BaixarEncomendaRetiradaUseCase;
import br.com.condominio.servico.encomenda.application.port.in.ReceberEncomendaUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portaria/encomendas")
public class PortariaEncomendaController {

  private static final Logger log = LoggerFactory.getLogger(PortariaEncomendaController.class);

  private final ReceberEncomendaUseCase receberEncomendaUseCase;
  private final BaixarEncomendaRetiradaUseCase baixarEncomendaRetiradaUseCase;

  public PortariaEncomendaController(
      ReceberEncomendaUseCase receberEncomendaUseCase,
      BaixarEncomendaRetiradaUseCase baixarEncomendaRetiradaUseCase
  ) {
    this.receberEncomendaUseCase = receberEncomendaUseCase;
    this.baixarEncomendaRetiradaUseCase = baixarEncomendaRetiradaUseCase;
  }

  @PostMapping
  public ResponseEntity<EncomendaResponse> receber(
      @Valid @RequestBody ReceberEncomendaRequest request,
      @AuthenticationPrincipal Jwt jwt
  ) {
    String recebidoPor = jwt == null ? null : jwt.getSubject();
    log.info("package receiving requested for apartment {}-{}", request.bloco(), request.apartamento());

    ReceberEncomendaUseCase.Result result = receberEncomendaUseCase.executar(
        new ReceberEncomendaUseCase.Command(
            request.nomeDestinatario(),
            request.apartamento(),
            request.bloco(),
            request.descricao(),
            recebidoPor
        )
    );

    log.info("package receiving registered with id {}", result.id());
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
  }

  @PostMapping("/{id}/retirada")
  public ResponseEntity<BaixaEncomendaResponse> baixarRetirada(
      @PathVariable("id") Long id,
      @Valid @RequestBody BaixarEncomendaRetiradaRequest request
  ) {
    log.info("package pickup requested for id {}", id);

    BaixarEncomendaRetiradaUseCase.Result result = baixarEncomendaRetiradaUseCase.executar(
        new BaixarEncomendaRetiradaUseCase.Command(
            id,
            request.retiradoPorNome()
        )
    );

    log.info("package pickup registered for id {}", result.encomendaId());
    return ResponseEntity.ok(toBaixaResponse(result));
  }

  private EncomendaResponse toResponse(ReceberEncomendaUseCase.Result result) {
    return new EncomendaResponse(
        result.id(),
        result.nomeDestinatario(),
        result.apartamento(),
        result.bloco(),
        result.descricao(),
        result.recebidoPor(),
        result.status(),
        result.dataRecebimento()
    );
  }

  private BaixaEncomendaResponse toBaixaResponse(BaixarEncomendaRetiradaUseCase.Result result) {
    return new BaixaEncomendaResponse(
        result.encomendaId(),
        result.status(),
        result.dataRetirada(),
        result.retiradoPorNome()
    );
  }
}
