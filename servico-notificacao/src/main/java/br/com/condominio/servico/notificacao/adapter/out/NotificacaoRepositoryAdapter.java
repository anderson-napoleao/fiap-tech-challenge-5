package br.com.condominio.servico.notificacao.adapter.out;

import br.com.condominio.servico.notificacao.application.port.out.NotificacaoRepositoryPort;
import br.com.condominio.servico.notificacao.domain.Notificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import br.com.condominio.servico.notificacao.infrastructure.persistence.entity.NotificacaoEntity;
import br.com.condominio.servico.notificacao.infrastructure.persistence.repository.SpringDataNotificacaoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Adaptador de saida para persistencia ou integracao externa.
 */
@Component
public class NotificacaoRepositoryAdapter implements NotificacaoRepositoryPort {

  private final SpringDataNotificacaoRepository notificacaoRepository;

  public NotificacaoRepositoryAdapter(SpringDataNotificacaoRepository notificacaoRepository) {
    this.notificacaoRepository = notificacaoRepository;
  }

  @Override
  public Notificacao salvar(Notificacao notificacao) {
    if (notificacao == null) {
      throw new IllegalArgumentException("Notificacao obrigatoria");
    }

    NotificacaoEntity entity = toEntity(notificacao);
    if (entity.getId() == null || entity.getId().isBlank()) {
      entity.setId(UUID.randomUUID().toString());
    }

    NotificacaoEntity salva = notificacaoRepository.save(entity);
    return toDomain(salva);
  }

  @Override
  public Optional<Notificacao> buscarPorId(String notificacaoId) {
    if (notificacaoId == null || notificacaoId.isBlank()) {
      throw new IllegalArgumentException("Notificacao obrigatoria");
    }
    return notificacaoRepository.findById(notificacaoId).map(this::toDomain);
  }

  @Override
  public List<Notificacao> listarNaoConfirmadasPorMorador(String moradorId, int page, int size) {
    if (moradorId == null || moradorId.isBlank()) {
      throw new IllegalArgumentException("Morador obrigatorio");
    }
    if (page < 0) {
      throw new IllegalArgumentException("Page invalida");
    }
    if (size <= 0) {
      throw new IllegalArgumentException("Size invalido");
    }

    return notificacaoRepository.findByMoradorIdAndStatusNot(
            moradorId,
            StatusNotificacao.CONFIRMADA,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        )
        .stream()
        .map(this::toDomain)
        .toList();
  }

  private NotificacaoEntity toEntity(Notificacao notificacao) {
    NotificacaoEntity entity = new NotificacaoEntity();
    entity.setId(notificacao.id());
    entity.setEncomendaId(notificacao.encomendaId());
    entity.setMoradorId(notificacao.moradorId());
    entity.setCanal(notificacao.canal());
    entity.setDestino(notificacao.destino());
    entity.setMensagem(notificacao.mensagem());
    entity.setStatus(notificacao.status());
    entity.setSourceEventId(notificacao.sourceEventId());
    entity.setCorrelationId(notificacao.correlationId());
    entity.setCreatedAt(notificacao.criadaEm());
    entity.setSentAt(notificacao.enviadaEm());
    entity.setConfirmedAt(notificacao.confirmadaEm());
    entity.setFailedAt(notificacao.falhaEm());
    entity.setFailureReason(notificacao.motivoFalha());
    return entity;
  }

  private Notificacao toDomain(NotificacaoEntity entity) {
    Notificacao notificacao = Notificacao.criar(
        entity.getEncomendaId(),
        entity.getMoradorId(),
        entity.getCanal(),
        entity.getDestino(),
        entity.getMensagem(),
        entity.getSourceEventId(),
        entity.getCorrelationId(),
        entity.getCreatedAt()
    ).atribuirId(entity.getId());

    aplicarStatus(notificacao, entity);
    return notificacao;
  }

  private void aplicarStatus(Notificacao notificacao, NotificacaoEntity entity) {
    if (entity.getStatus() == null || entity.getStatus() == StatusNotificacao.PENDENTE) {
      return;
    }

    if (entity.getStatus() == StatusNotificacao.ENVIADA) {
      notificacao.marcarEnviada(valorOuCriadaEm(entity.getSentAt(), entity));
      return;
    }

    if (entity.getStatus() == StatusNotificacao.FALHA) {
      notificacao.marcarFalha(
          valorOuPadrao(entity.getFailureReason(), "Falha de envio"),
          valorOuCriadaEm(entity.getFailedAt(), entity)
      );
      return;
    }

    if (entity.getStatus() == StatusNotificacao.CONFIRMADA) {
      notificacao.marcarEnviada(valorOuCriadaEm(entity.getSentAt(), entity));
      notificacao.confirmarRecebimento(
          entity.getMoradorId(),
          valorOuCriadaEm(entity.getConfirmedAt(), entity)
      );
    }
  }

  private Instant valorOuCriadaEm(Instant valor, NotificacaoEntity entity) {
    if (valor != null) {
      return valor;
    }
    if (entity.getCreatedAt() != null) {
      return entity.getCreatedAt();
    }
    throw new IllegalStateException("createdAt obrigatorio para reconstruir notificacao");
  }

  private String valorOuPadrao(String valor, String padrao) {
    return (valor == null || valor.isBlank()) ? padrao : valor;
  }
}
