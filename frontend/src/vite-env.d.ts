/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_IDENTITY_URL?: string
  readonly VITE_API_USUARIO_URL?: string
  readonly VITE_API_ENCOMENDA_URL?: string
  readonly VITE_API_NOTIFICACAO_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
