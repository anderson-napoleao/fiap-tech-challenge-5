package br.com.condominio.servico.encomenda.adapter.in.web;

import br.com.condominio.servico.encomenda.adapter.in.web.dto.BaixaEncomendaResponse;
import br.com.condominio.servico.encomenda.adapter.in.web.dto.BaixarEncomendaRetiradaRequest;
import br.com.condominio.servico.encomenda.adapter.in.web.dto.EncomendaResponse;
import br.com.condominio.servico.encomenda.adapter.in.web.dto.ListarEncomendasResponse;
import br.com.condominio.servico.encomenda.adapter.in.web.dto.ReceberEncomendaRequest;
import br.com.condominio.servico.encomenda.application.port.in.BaixarEncomendaRetiradaUseCase;
import br.com.condominio.servico.encomenda.application.port.in.BuscarEncomendaPorIdUseCase;
import br.com.condominio.servico.encomenda.application.port.in.ListarEncomendasPortariaUseCase;
import br.com.condominio.servico.encomenda.application.port.in.ReceberEncomendaUseCase;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador HTTP de entrada que delega para casos de uso.
 */
@RestController
@RequestMapping("/portaria/encomendas")
public class PortariaEncomendaController {

  private static final Logger log = LoggerFactory.getLogger(PortariaEncomendaController.class);

  private final ReceberEncomendaUseCase receberEncomendaUseCase;
  private final BaixarEncomendaRetiradaUseCase baixarEncomendaRetiradaUseCase;
  private final BuscarEncomendaPorIdUseCase buscarEncomendaPorIdUseCase;
  private final ListarEncomendasPortariaUseCase listarEncomendasPortariaUseCase;

  public PortariaEncomendaController(
      ReceberEncomendaUseCase receberEncomendaUseCase,
      BaixarEncomendaRetiradaUseCase baixarEncomendaRetiradaUseCase,
      BuscarEncomendaPorIdUseCase buscarEncomendaPorIdUseCase,
      ListarEncomendasPortariaUseCase listarEncomendasPortariaUseCase
  ) {
    this.receberEncomendaUseCase = receberEncomendaUseCase;
    this.baixarEncomendaRetiradaUseCase = baixarEncomendaRetiradaUseCase;
    this.buscarEncomendaPorIdUseCase = buscarEncomendaPorIdUseCase;
    this.listarEncomendasPortariaUseCase = listarEncomendasPortariaUseCase;
  }

  @GetMapping
  public ResponseEntity<ListarEncomendasResponse> listar(
      @RequestParam(name = "apartamento", required = false) String apartamento,
      @RequestParam(name = "bloco", required = false) String bloco,
      @RequestParam(name = "data", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size
  ) {
    log.info(
        "package listing requested with filters apartment {} block {} date {} page {} size {}",
        apartamento,
        bloco,
        data,
        page,
        size
    );

    ListarEncomendasPortariaUseCase.Result result = listarEncomendasPortariaUseCase.executar(
        new ListarEncomendasPortariaUseCase.Command(apartamento, bloco, data, page, size)
    );

    List<EncomendaResponse> encomendas = result.encomendas().stream()
        .map(this::toResponse)
        .toList();

    return ResponseEntity.ok(
        new ListarEncomendasResponse(
            encomendas,
            result.page(),
            result.size(),
            result.totalElements(),
            result.totalPages()
        )
    );
  }

  @GetMapping("/{id}")
  public ResponseEntity<EncomendaResponse> buscarPorId(@PathVariable("id") Long id) {
    log.info("package query requested for id {}", id);

    BuscarEncomendaPorIdUseCase.Result result = buscarEncomendaPorIdUseCase.executar(
        new BuscarEncomendaPorIdUseCase.Command(id)
    );

    return ResponseEntity.ok(toResponse(result));
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
        result.dataRecebimento(),
        null,
        null
    );
  }

  private EncomendaResponse toResponse(BuscarEncomendaPorIdUseCase.Result result) {
    return new EncomendaResponse(
        result.id(),
        result.nomeDestinatario(),
        result.apartamento(),
        result.bloco(),
        result.descricao(),
        result.recebidoPor(),
        result.status(),
        result.dataRecebimento(),
        result.dataRetirada(),
        result.retiradoPorNome()
    );
  }

  private EncomendaResponse toResponse(ListarEncomendasPortariaUseCase.Item result) {
    return new EncomendaResponse(
        result.id(),
        result.nomeDestinatario(),
        result.apartamento(),
        result.bloco(),
        result.descricao(),
        result.recebidoPor(),
        result.status(),
        result.dataRecebimento(),
        result.dataRetirada(),
        result.retiradoPorNome()
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
