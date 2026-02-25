CREATE INDEX idx_encomendas_bloco_apto_data
  ON encomendas (bloco, apartamento, data_recebimento DESC);
