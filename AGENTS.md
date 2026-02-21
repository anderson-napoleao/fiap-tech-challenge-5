# Regras de Arquitetura

- Este projeto segue `Clean Architecture`.
- A camada `domain` deve ser pura.
- A camada `domain` nao pode depender de Spring, Spring Boot ou qualquer framework.
- A camada `domain` pode depender apenas de JDK e codigo do proprio dominio.
- A camada `application` pode depender de `domain`.
- A camada `application` nao pode depender de Spring, Spring Boot ou qualquer framework.
- A camada `application` deve acessar integracoes externas somente via portas/interfaces.
