# frontend

## Resumo

Aplicação web que centraliza os fluxos de usuário do sistema condominial:

- cadastro e login
- visualização e atualização de dados do morador
- recebimento e retirada de encomendas
- consulta e confirmação de notificações

## Tecnologias e ferramentas

- React 18
- TypeScript
- Vite
- React Router
- React Query
- React Hook Form
- Axios
- Tailwind CSS
- Nginx (entrega da SPA em container)

## Organização da aplicação

- `pages`: telas por caso de uso
- `components`: componentes reutilizáveis
- `services`: integração HTTP com backend
- `types` e `utils`: contratos e utilitários

## Desafios e soluções

1. Integrar múltiplas APIs com perfis de acesso diferentes.
- Solução: camada de serviços HTTP separada por domínio funcional.

2. Entrega de SPA em Docker com roteamento correto.
- Solução: build com Vite e Nginx com fallback em `index.html`.

3. Consistência visual e de estado.
- Solução: componentes reutilizáveis e padronização de fluxo de requisição.

## Execução local

```powershell
cd frontend
npm ci
npm run dev
```

Aplicação: `http://localhost:5173`

## Execução via Docker

No diretório raiz do projeto:

```powershell
docker compose up -d frontend
```

Aplicação: `http://localhost:3000`

## Build

```powershell
cd frontend
npm run build
```
