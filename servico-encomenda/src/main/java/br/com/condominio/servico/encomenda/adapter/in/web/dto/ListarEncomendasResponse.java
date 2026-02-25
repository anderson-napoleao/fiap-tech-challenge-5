package br.com.condominio.servico.encomenda.adapter.in.web.dto;

import java.util.List;

/**
 * DTO usado para entrada e saida da API HTTP.
 */
public record ListarEncomendasResponse(
    List<EncomendaResponse> encomendas,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
