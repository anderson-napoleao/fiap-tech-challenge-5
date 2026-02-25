package br.com.condominio.servico.encomenda.application.port.out;

import br.com.condominio.servico.encomenda.domain.Encomenda;
import java.util.Optional;

/**
 * Define o contrato de saida da aplicacao para integracoes externas.
 */
public interface EncomendaRepositoryPort {

  Optional<Encomenda> buscarPorId(Long id);

  Encomenda salvar(Encomenda encomenda);
}
