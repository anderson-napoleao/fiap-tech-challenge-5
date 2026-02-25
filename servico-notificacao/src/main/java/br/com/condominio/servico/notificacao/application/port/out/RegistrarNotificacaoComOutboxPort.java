package br.com.condominio.servico.notificacao.application.port.out;

import br.com.condominio.servico.notificacao.domain.Notificacao;

public interface RegistrarNotificacaoComOutboxPort {

  Notificacao registrar(Notificacao notificacao);
}
