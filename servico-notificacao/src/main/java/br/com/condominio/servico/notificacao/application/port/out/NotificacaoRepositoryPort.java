package br.com.condominio.servico.notificacao.application.port.out;

import br.com.condominio.servico.notificacao.domain.Notificacao;
import java.util.List;
import java.util.Optional;

/**
 * Define o contrato de saida da aplicacao para integracoes externas.
 */
public interface NotificacaoRepositoryPort {

  Notificacao salvar(Notificacao notificacao);

  Optional<Notificacao> buscarPorId(String notificacaoId);

  List<Notificacao> listarNaoConfirmadasPorMorador(String moradorId, int page, int size);
}
