FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Parent and module descriptors
COPY pom.xml ./pom.xml
COPY servico-identidade/pom.xml ./servico-identidade/pom.xml
COPY servico-usuario/pom.xml ./servico-usuario/pom.xml
COPY servico-encomenda/pom.xml ./servico-encomenda/pom.xml
COPY servico-notificacao/pom.xml ./servico-notificacao/pom.xml
COPY tests-integracao-sistema/pom.xml ./tests-integracao-sistema/pom.xml

# Source code required for javadoc generation
COPY servico-identidade/src ./servico-identidade/src
COPY servico-usuario/src ./servico-usuario/src
COPY servico-encomenda/src ./servico-encomenda/src
COPY servico-notificacao/src ./servico-notificacao/src

RUN mvn -pl servico-identidade,servico-usuario,servico-encomenda,servico-notificacao \
    -am -DskipTests javadoc:javadoc -B

RUN mkdir -p /out/javadocs && \
    cp -r servico-identidade/target/reports/apidocs /out/javadocs/servico-identidade && \
    cp -r servico-usuario/target/reports/apidocs /out/javadocs/servico-usuario && \
    cp -r servico-encomenda/target/reports/apidocs /out/javadocs/servico-encomenda && \
    cp -r servico-notificacao/target/reports/apidocs /out/javadocs/servico-notificacao

COPY DOCKER.md /out/DOCKER.md

RUN cat > /out/index.html <<'EOF'
<!doctype html>
<html lang="pt-BR">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Documentacao Tecnica</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 40px; color: #111827; }
    h1 { margin-bottom: 8px; }
    .muted { color: #6b7280; margin-bottom: 24px; }
    ul { line-height: 1.9; }
    a { color: #1d4ed8; text-decoration: none; }
    a:hover { text-decoration: underline; }
  </style>
</head>
<body>
  <h1>Documentacao Tecnica</h1>
  <p class="muted">JavaDoc por servico e guia Docker.</p>
  <h2>JavaDoc</h2>
  <ul>
    <li><a href="/javadocs/servico-identidade/index.html">Servico Identidade</a></li>
    <li><a href="/javadocs/servico-usuario/index.html">Servico Usuario</a></li>
    <li><a href="/javadocs/servico-encomenda/index.html">Servico Encomenda</a></li>
    <li><a href="/javadocs/servico-notificacao/index.html">Servico Notificacao</a></li>
  </ul>
  <h2>Docker</h2>
  <ul>
    <li><a href="/DOCKER.md">Guia Docker (DOCKER.md)</a></li>
  </ul>
</body>
</html>
EOF

FROM nginx:alpine
COPY --from=builder /out /usr/share/nginx/html
EXPOSE 80
