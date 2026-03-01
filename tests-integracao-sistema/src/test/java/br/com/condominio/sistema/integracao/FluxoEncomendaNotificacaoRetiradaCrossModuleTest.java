package br.com.condominio.sistema.integracao;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;

import br.com.condominio.identidade.ServicoIdentidadeApplication;
import br.com.condominio.servico.encomenda.ServicoEncomendaApplication;
import br.com.condominio.servico.notificacao.ServicoNotificacaoApplication;
import br.com.condominio.servico.usuario.ServicoUsuarioApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@EnabledIfSystemProperty(named = "testcontainers.enabled", matches = "true")
class FluxoEncomendaNotificacaoRetiradaCrossModuleTest {

  private static final String JWT_SECRET = "condominio-jwt-secret-local-2026-seguro";
  private static final String JWT_ISSUER = "servico-identidade";

  static PostgreSQLContainer<?> identidadeDb = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("condominio_identidade")
      .withUsername("postgres")
      .withPassword("postgres");

  static PostgreSQLContainer<?> usuarioDb = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("condominio_usuario")
      .withUsername("postgres")
      .withPassword("postgres");

  static PostgreSQLContainer<?> encomendaDb = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("condominio")
      .withUsername("postgres")
      .withPassword("postgres");

  static PostgreSQLContainer<?> notificacaoDb = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("condominio_notificacao")
      .withUsername("postgres")
      .withPassword("postgres");

  static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static ConfigurableApplicationContext identidadeContext;
  private static ConfigurableApplicationContext usuarioContext;
  private static ConfigurableApplicationContext encomendaContext;
  private static ConfigurableApplicationContext notificacaoContext;

  private static int identidadePort;
  private static int usuarioPort;
  private static int encomendaPort;
  private static int notificacaoPort;

  @BeforeAll
  static void startApplications() {
    Assumptions.assumeTrue(isDockerAvailable(), "Docker indisponivel para Testcontainers");

    identidadeDb.start();
    usuarioDb.start();
    encomendaDb.start();
    notificacaoDb.start();
    kafka.start();

    identidadeContext = new SpringApplicationBuilder(ServicoIdentidadeApplication.class)
        .properties(Map.ofEntries(
            Map.entry("spring.config.location", "optional:classpath:/nao-existe/"),
            Map.entry("server.port", "0"),
            Map.entry("spring.application.name", "servico-identidade"),
            Map.entry("spring.datasource.url", identidadeDb.getJdbcUrl()),
            Map.entry("spring.datasource.username", identidadeDb.getUsername()),
            Map.entry("spring.datasource.password", identidadeDb.getPassword()),
            Map.entry("spring.datasource.driver-class-name", "org.postgresql.Driver"),
            Map.entry("spring.jpa.hibernate.ddl-auto", "validate"),
            Map.entry("spring.jpa.open-in-view", "false"),
            Map.entry("spring.flyway.enabled", "true"),
            Map.entry("spring.flyway.locations", migrationLocation("servico-identidade")),
            Map.entry("security.jwt.secret", JWT_SECRET),
            Map.entry("security.jwt.issuer", JWT_ISSUER),
            Map.entry("security.jwt.expires-seconds", "3600"),
            Map.entry("management.endpoints.web.exposure.include", "health,info")
        ))
        .run();
    identidadePort = webServerPort(identidadeContext);

    usuarioContext = new SpringApplicationBuilder(ServicoUsuarioApplication.class)
        .properties(Map.ofEntries(
            Map.entry("spring.config.location", "optional:classpath:/nao-existe/"),
            Map.entry("server.port", "0"),
            Map.entry("spring.application.name", "servico-usuario"),
            Map.entry("spring.datasource.url", usuarioDb.getJdbcUrl()),
            Map.entry("spring.datasource.username", usuarioDb.getUsername()),
            Map.entry("spring.datasource.password", usuarioDb.getPassword()),
            Map.entry("spring.datasource.driver-class-name", "org.postgresql.Driver"),
            Map.entry("spring.jpa.hibernate.ddl-auto", "validate"),
            Map.entry("spring.jpa.open-in-view", "false"),
            Map.entry("spring.flyway.enabled", "true"),
            Map.entry("spring.flyway.locations", migrationLocation("servico-usuario")),
            Map.entry("security.jwt.secret", JWT_SECRET),
            Map.entry("security.jwt.issuer", JWT_ISSUER),
            Map.entry("identity.base-url", "http://localhost:" + identidadePort),
            Map.entry("identity.admin.username", "admin"),
            Map.entry("identity.admin.password", "admin"),
            Map.entry("management.endpoints.web.exposure.include", "health,info")
        ))
        .run();
    usuarioPort = webServerPort(usuarioContext);

    encomendaContext = new SpringApplicationBuilder(ServicoEncomendaApplication.class)
        .properties(Map.ofEntries(
            Map.entry("spring.config.location", "optional:classpath:/nao-existe/"),
            Map.entry("server.port", "0"),
            Map.entry("spring.application.name", "servico-encomenda"),
            Map.entry("spring.datasource.url", encomendaDb.getJdbcUrl()),
            Map.entry("spring.datasource.username", encomendaDb.getUsername()),
            Map.entry("spring.datasource.password", encomendaDb.getPassword()),
            Map.entry("spring.datasource.driver-class-name", "org.postgresql.Driver"),
            Map.entry("spring.jpa.hibernate.ddl-auto", "none"),
            Map.entry("spring.jpa.open-in-view", "false"),
            Map.entry("spring.flyway.enabled", "true"),
            Map.entry("spring.flyway.locations", migrationLocation("servico-encomenda")),
            Map.entry("security.jwt.secret", JWT_SECRET),
            Map.entry("security.jwt.issuer", JWT_ISSUER),
            Map.entry("management.endpoints.web.exposure.include", "health,info")
        ))
        .run();
    encomendaPort = webServerPort(encomendaContext);

    notificacaoContext = new SpringApplicationBuilder(ServicoNotificacaoApplication.class)
        .properties(Map.ofEntries(
            Map.entry("spring.config.location", "optional:classpath:/nao-existe/"),
            Map.entry("server.port", "0"),
            Map.entry("spring.application.name", "servico-notificacao"),
            Map.entry("spring.datasource.url", notificacaoDb.getJdbcUrl()),
            Map.entry("spring.datasource.username", notificacaoDb.getUsername()),
            Map.entry("spring.datasource.password", notificacaoDb.getPassword()),
            Map.entry("spring.datasource.driver-class-name", "org.postgresql.Driver"),
            Map.entry("spring.jpa.hibernate.ddl-auto", "none"),
            Map.entry("spring.jpa.open-in-view", "false"),
            Map.entry("spring.flyway.enabled", "true"),
            Map.entry("spring.flyway.locations", migrationLocation("servico-notificacao")),
            Map.entry("spring.kafka.bootstrap-servers", kafka.getBootstrapServers()),
            Map.entry("security.jwt.secret", JWT_SECRET),
            Map.entry("security.jwt.issuer", JWT_ISSUER),
            Map.entry("identity.base-url", "http://localhost:" + identidadePort),
            Map.entry("identity.service-username", "admin"),
            Map.entry("identity.service-password", "admin"),
            Map.entry("usuario.base-url", "http://localhost:" + usuarioPort),
            Map.entry("app.kafka.topics.encomenda-recebida", "encomenda.recebida"),
            Map.entry("management.endpoints.web.exposure.include", "health,info")
        ))
        .run();
    notificacaoPort = webServerPort(notificacaoContext);
  }

  @AfterAll
  static void stopApplications() {
    if (notificacaoContext != null) {
      notificacaoContext.close();
    }
    if (encomendaContext != null) {
      encomendaContext.close();
    }
    if (usuarioContext != null) {
      usuarioContext.close();
    }
    if (identidadeContext != null) {
      identidadeContext.close();
    }

    notificacaoDb.stop();
    encomendaDb.stop();
    usuarioDb.stop();
    identidadeDb.stop();
    kafka.stop();
  }

  @Test
  void deveExecutarFluxoCrossModuleDeEncomendaNotificacaoEretirada() throws Exception {
    String moradorEmail = "morador.fluxo." + System.nanoTime() + "@teste.com";
    String funcionarioEmail = "func.fluxo." + System.nanoTime() + "@teste.com";
    String senha = "Senha@123";

    cadastrarMorador(moradorEmail, senha);
    cadastrarFuncionario(funcionarioEmail, senha);

    String tokenMorador = autenticar(moradorEmail, senha);
    String tokenFuncionario = autenticar(funcionarioEmail, senha);
    String moradorIdentityId = extrairSubjectDoToken(tokenMorador);

    Number encomendaIdResposta = given()
        .baseUri("http://localhost:" + encomendaPort)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + tokenFuncionario)
        .body(Map.of(
            "nomeDestinatario", "Maria Moradora",
            "apartamento", "101",
            "bloco", "A",
            "descricao", "Caixa do marketplace"
        ))
        .when()
        .post("/portaria/encomendas")
        .then()
        .statusCode(201)
        .body("status", equalTo("RECEBIDA"))
        .extract()
        .path("id");
    long encomendaId = encomendaIdResposta.longValue();

    publicarEventoEncomendaRecebida(encomendaId);

    String notificacaoId = aguardarNotificacaoPendente(tokenMorador);

    given()
        .baseUri("http://localhost:" + notificacaoPort)
        .header("Authorization", "Bearer " + tokenMorador)
        .when()
        .post("/morador/notificacoes/" + notificacaoId + "/confirmacao")
        .then()
        .statusCode(200)
        .body("id", equalTo(notificacaoId))
        .body("status", equalTo("CONFIRMADA"));

    given()
        .baseUri("http://localhost:" + encomendaPort)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + tokenFuncionario)
        .body(Map.of("retiradoPorNome", "Maria Moradora"))
        .when()
        .post("/portaria/encomendas/" + encomendaId + "/retirada")
        .then()
        .statusCode(200)
        .body("id", equalTo((int) encomendaId))
        .body("status", equalTo("RETIRADA"))
        .body("retiradoPorNome", equalTo("Maria Moradora"));

    given()
        .baseUri("http://localhost:" + notificacaoPort)
        .header("Authorization", "Bearer " + tokenMorador)
        .queryParam("confirmada", false)
        .when()
        .get("/morador/notificacoes")
        .then()
        .statusCode(200)
        .body("notificacoes.size()", equalTo(0));

    given()
        .baseUri("http://localhost:" + notificacaoPort)
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + tokenFuncionario)
        .when()
        .post("/morador/notificacoes/" + notificacaoId + "/confirmacao")
        .then()
        .statusCode(403);

    given()
        .baseUri("http://localhost:" + usuarioPort)
        .header("Authorization", "Bearer " + tokenMorador)
        .when()
        .get("/users/me")
        .then()
        .statusCode(200)
        .body("identityId", equalTo(moradorIdentityId));
  }

  private void cadastrarMorador(String email, String senha) {
    given()
        .baseUri("http://localhost:" + usuarioPort)
        .contentType(ContentType.JSON)
        .body(Map.of(
            "nomeCompleto", "Maria Moradora",
            "email", email,
            "senha", senha,
            "tipo", "MORADOR",
            "telefone", "11999990000",
            "cpf", "12345678901",
            "apartamento", "101",
            "bloco", "A"
        ))
        .when()
        .post("/users")
        .then()
        .statusCode(201)
        .body("email", equalTo(email))
        .body("identityId", not(blankOrNullString()));
  }

  private void cadastrarFuncionario(String email, String senha) {
    given()
        .baseUri("http://localhost:" + usuarioPort)
        .contentType(ContentType.JSON)
        .body(Map.of(
            "nomeCompleto", "Paulo Porteiro",
            "email", email,
            "senha", senha,
            "tipo", "FUNCIONARIO",
            "telefone", "11988887777",
            "cpf", "98765432100"
        ))
        .when()
        .post("/users")
        .then()
        .statusCode(201)
        .body("email", equalTo(email))
        .body("identityId", not(blankOrNullString()));
  }

  private String autenticar(String email, String senha) {
    return given()
        .baseUri("http://localhost:" + identidadePort)
        .contentType(ContentType.JSON)
        .body(Map.of(
            "username", email,
            "password", senha
        ))
        .when()
        .post("/auth/token")
        .then()
        .statusCode(200)
        .body("token_type", equalTo("Bearer"))
        .body("expires_in", greaterThan(0))
        .extract()
        .path("access_token");
  }

  private void publicarEventoEncomendaRecebida(long encomendaId) throws Exception {
    String payload = OBJECT_MAPPER.writeValueAsString(Map.of(
        "eventId", "evt-" + System.nanoTime(),
        "eventVersion", 1,
        "occurredAt", Instant.now().toString(),
        "encomendaId", encomendaId,
        "nomeDestinatario", "Maria Moradora",
        "apartamento", "101",
        "bloco", "A",
        "descricao", "Caixa do marketplace",
        "recebidoPor", "porteiro-1",
        "status", "RECEBIDA"
    ));

    Properties properties = new Properties();
    properties.put("bootstrap.servers", kafka.getBootstrapServers());
    properties.put("key.serializer", StringSerializer.class.getName());
    properties.put("value.serializer", StringSerializer.class.getName());
    properties.put("acks", "all");

    try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties)) {
      producer.send(new ProducerRecord<>("encomenda.recebida", String.valueOf(encomendaId), payload))
          .get(10, TimeUnit.SECONDS);
      producer.flush();
    }
  }

  private String aguardarNotificacaoPendente(String tokenMorador) throws InterruptedException {
    long timeoutAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);

    while (System.currentTimeMillis() < timeoutAt) {
      String notificacaoId = given()
          .baseUri("http://localhost:" + notificacaoPort)
          .header("Authorization", "Bearer " + tokenMorador)
          .queryParam("confirmada", false)
          .when()
          .get("/morador/notificacoes")
          .then()
          .statusCode(200)
          .extract()
          .path("notificacoes[0].id");

      if (notificacaoId != null && !notificacaoId.isBlank()) {
        return notificacaoId;
      }

      Thread.sleep(500);
    }

    throw new AssertionError("Notificacao pendente nao foi criada em tempo habil");
  }

  private String extrairSubjectDoToken(String jwt) {
    String[] parts = jwt.split("\\.");
    if (parts.length < 2) {
      throw new IllegalArgumentException("Token JWT invalido");
    }
    return decodificarSubject(parts[1]);
  }

  private String decodificarSubject(String payloadPart) {
    try {
      String normalized = payloadPart.replace('-', '+').replace('_', '/');
      int remainder = normalized.length() % 4;
      if (remainder != 0) {
        normalized = normalized + "=".repeat(4 - remainder);
      }
      byte[] decoded = java.util.Base64.getDecoder().decode(normalized);
      Map<?, ?> claims = OBJECT_MAPPER.readValue(decoded, Map.class);
      Object subject = claims.get("sub");
      if (subject == null || String.valueOf(subject).isBlank()) {
        throw new IllegalStateException("JWT sem claim sub");
      }
      return String.valueOf(subject);
    } catch (Exception exception) {
      throw new IllegalStateException("Falha ao extrair sub do JWT", exception);
    }
  }

  private static int webServerPort(ConfigurableApplicationContext context) {
    return ((ServletWebServerApplicationContext) context).getWebServer().getPort();
  }

  private static String migrationLocation(String moduleName) {
    Path migrationPath = Path.of("..", moduleName, "src", "main", "resources", "db", "migration")
        .toAbsolutePath()
        .normalize();
    return "filesystem:" + migrationPath.toString().replace("\\", "/");
  }

  private static boolean isDockerAvailable() {
    try {
      return DockerClientFactory.instance().isDockerAvailable();
    } catch (RuntimeException exception) {
      return false;
    }
  }
}
