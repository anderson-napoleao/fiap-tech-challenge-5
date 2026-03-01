# Sistema Condominio - Versao para Avaliacao com Prints

Este documento e uma versao orientada a apresentacao visual.  
Os pontos de captura estao definidos para facilitar a montagem do relatorio final.

> Caminho recomendado para salvar imagens: `docs/prints/`.

## 1. Visao Geral da Solucao

O sistema e composto por quatro microservicos de backend, um frontend web, mensageria com Kafka/CDC e observabilidade com ELK.

![Arquitetura Geral](docs/prints/01-arquitetura-geral.png)

## 2. Servicos de Aplicacao

### 2.1 servico-identidade

- Emite JWT.
- Administra identidades e papeis.

![Swagger Identidade](docs/prints/02-swagger-identidade.png)

### 2.2 servico-usuario

- Cadastro de usuario e consulta de perfil.

![Swagger Usuario](docs/prints/03-swagger-usuario.png)

### 2.3 servico-encomenda

- Registro de recebimento e retirada.
- Publicacao de evento por outbox.

![Swagger Encomenda](docs/prints/04-swagger-encomenda.png)

### 2.4 servico-notificacao

- Processamento de evento e notificacao ao morador.

![Swagger Notificacao](docs/prints/05-swagger-notificacao.png)

### 2.5 frontend

- Interface para demonstrar fluxo completo.

![Dashboard Frontend](docs/prints/06-frontend-dashboard.png)

## 3. Docker em Execucao

Subida:

```bash
docker compose up -d --build
docker compose ps
```

Print sugerido:

![Containers em Execucao](docs/prints/07-docker-compose-ps.png)

## 4. Fluxo Funcional no Frontend

### 4.1 Cadastro de morador

![Cadastro Morador](docs/prints/08-frontend-cadastro-morador.png)

### 4.2 Cadastro/login de funcionario

![Cadastro Funcionario](docs/prints/09-frontend-cadastro-funcionario.png)

### 4.3 Recebimento de encomenda

![Receber Encomenda](docs/prints/10-frontend-receber-encomenda.png)

### 4.4 Confirmacao de notificacao pelo morador

![Confirmar Notificacao](docs/prints/11-frontend-confirmar-notificacao.png)

## 5. Observabilidade (Kibana)

Passos:

1. Acessar `http://localhost:5601`.
2. Abrir `Discover`.
3. Filtrar `service.name`.

Print sugerido:

![Kibana Discover](docs/prints/12-kibana-discover.png)

## 6. Verificacao em Banco de Dados (Adminer)

Passos:

1. Acessar `http://localhost:8087`.
2. Conectar no banco desejado.
3. Rodar queries de validacao.

Print sugerido:

![Adminer Consulta](docs/prints/13-adminer-consulta.png)

## 7. Verificacao em Kafka (Kafka UI)

Passos:

1. Acessar `http://localhost:8086`.
2. Abrir topico `encomenda.recebida`.
3. Inspecionar mensagens.

Print sugerido:

![Kafka UI Topico](docs/prints/14-kafka-ui-topico.png)

## 8. Execucao de Testes

```bash
mvn test
mvn test -Dtestcontainers.enabled=true
mvn -pl tests-integracao-sistema -am test -Dtestcontainers.enabled=true -Dtest=FluxoCadastroUsuarioCrossModuleTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -Pquality verify
```

Print sugerido:

![Execucao de Testes](docs/prints/15-testes-maven.png)

## 9. Checklist para entrega da avaliacao

- [ ] Arquitetura explicada.
- [ ] Fluxo funcional demonstrado no frontend.
- [ ] Evidencia de evento no Kafka.
- [ ] Evidencia de persistencia no banco.
- [ ] Evidencia de logs no Kibana.
- [ ] Evidencia de testes.

