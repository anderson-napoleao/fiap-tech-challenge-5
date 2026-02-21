# Regras de Arquitetura

- Este projeto segue `Clean Architecture`.
- A camada `domain` deve ser pura.
- A camada `domain` nao pode depender de Spring, Spring Boot ou qualquer framework.
- A camada `domain` pode depender apenas de JDK e codigo do proprio dominio.
- A camada `application` pode depender de `domain`.
- A camada `application` nao pode depender de Spring, Spring Boot ou qualquer framework.
- A camada `application` deve acessar integracoes externas somente via portas/interfaces.

# Regras de Use Case e Validacao

- Todo `Command` de `UseCase` deve validar regras simples de campos no proprio construtor.
- Para chamar `UseCase`, sempre passar um `Command` valido como parametro de entrada.
- Regras de negocio devem ficar em entidade de dominio rica quando couber na entidade.
- Regras de negocio que envolvem mais de uma entidade devem ficar em servicos de dominio.
