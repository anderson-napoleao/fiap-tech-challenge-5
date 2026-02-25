package br.com.condominio.servico.notificacao.infrastructure.persistence.repository;

import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.com.condominio.servico.notificacao.infrastructure.persistence.entity.NotificacaoEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotificacaoRepository extends JpaRepository<NotificacaoEntity, String> {

  Optional<NotificacaoEntity> findBySourceEventId(String sourceEventId);

  Page<NotificacaoEntity> findByMoradorIdAndStatusNot(
      String moradorId,
      StatusNotificacao status,
      Pageable pageable
  );
}
