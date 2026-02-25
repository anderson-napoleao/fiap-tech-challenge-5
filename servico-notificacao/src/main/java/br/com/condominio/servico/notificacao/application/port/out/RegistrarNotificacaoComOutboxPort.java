package br.com.condominio.servico.notificacao.application.port.out;

import br.com.condominio.servico.notificacao.domain.Notificacao;

/**
 * Define o contrato de saida da aplicacao para integracoes externas.
 */
public interface RegistrarNotificacaoComOutboxPort {

  Notificacao registrar(Notificacao notificacao);
}
