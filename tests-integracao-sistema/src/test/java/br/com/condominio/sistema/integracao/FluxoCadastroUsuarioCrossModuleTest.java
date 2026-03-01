package br.com.condominio.sistema.integracao;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;

import br.com.condominio.identidade.ServicoIdentidadeApplication;
import br.com.condominio.servico.usuario.ServicoUsuarioApplication;
import io.restassured.http.ContentType;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

@EnabledIfSystemProperty(named = "testcontainers.enabled", matches = "true")
class FluxoCadastroUsuarioCrossModuleTest {

  static PostgreSQLContainer<?> identidadeDb = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("condominio_identidade")
      .withUsername("postgres")
      .withPassword("postgres");

  static PostgreSQLContainer<?> usuarioDb = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("condominio_usuario")
      .withUsername("postgres")
      .withPassword("postgres");

  private static ConfigurableApplicationContext identidadeContext;
  private static ConfigurableApplicationContext usuarioContext;
  private static int identidadePort;
  private static int usuarioPort;

  @BeforeAll
  static void startApplications() {
    Assumptions.assumeTrue(isDockerAvailable(), "Docker indisponivel para Testcontainers");

    identidadeDb.start();
    usuarioDb.start();

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
            Map.entry("security.jwt.secret", "condominio-jwt-secret-local-2026-seguro"),
            Map.entry("security.jwt.issuer", "servico-identidade"),
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
            Map.entry("security.jwt.secret", "condominio-jwt-secret-local-2026-seguro"),
            Map.entry("security.jwt.issuer", "servico-identidade"),
            Map.entry("identity.base-url", "http://localhost:" + identidadePort),
            Map.entry("identity.admin.username", "admin"),
            Map.entry("identity.admin.password", "admin"),
            Map.entry("management.endpoints.web.exposure.include", "health,info")
        ))
        .run();

    usuarioPort = webServerPort(usuarioContext);
  }

  @AfterAll
  static void stopApplications() {
    if (usuarioContext != null) {
      usuarioContext.close();
    }
    if (identidadeContext != null) {
      identidadeContext.close();
    }
    usuarioDb.stop();
    identidadeDb.stop();
  }

  @Test
  void deveCadastrarUsuarioGerarTokenEBuscarPerfilNoFluxoCrossModule() {
    String email = "cross.module." + System.nanoTime() + "@teste.com";
    String senha = "Senha@123";

    given()
        .baseUri("http://localhost:" + usuarioPort)
        .contentType(ContentType.JSON)
        .body(Map.of(
            "nomeCompleto", "Maria Cross Module",
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

    String token = given()
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
        .extract()
        .path("access_token");

    given()
        .baseUri("http://localhost:" + usuarioPort)
        .header("Authorization", "Bearer " + token)
        .when()
        .get("/users/me")
        .then()
        .statusCode(200)
        .body("email", equalTo(email))
        .body("nomeCompleto", equalTo("Maria Cross Module"));
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
