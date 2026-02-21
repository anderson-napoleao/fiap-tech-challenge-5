package br.com.condominio.servico.encomenda.application.port.out;

import br.com.condominio.servico.encomenda.domain.Encomenda;

public interface RegistrarRecebimentoComOutboxPort {

  Encomenda registrar(Encomenda encomenda);
}
