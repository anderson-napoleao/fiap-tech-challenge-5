package br.com.condominio.servico.encomenda.application.port.out;

import br.com.condominio.servico.encomenda.domain.Encomenda;

/**
 * Define o contrato de saida da aplicacao para integracoes externas.
 */
public interface RegistrarRecebimentoComOutboxPort {

  Encomenda registrar(Encomenda encomenda);
}
