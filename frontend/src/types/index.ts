// Tipos base da aplicação
export interface BaseEntity {
  id: string | number
}

// Tipos de Usuário
export enum TipoUsuario {
  MORADOR = 'MORADOR',
  FUNCIONARIO = 'FUNCIONARIO'
}

export interface Usuario extends BaseEntity {
  identityId: string
  nomeCompleto: string
  email: string
  tipo: TipoUsuario
  telefone?: string
  cpf?: string
  apartamento?: string
  bloco?: string
}

// Auth
export interface LoginRequest {
  username: string // email
  password: string
}

export interface LoginResponse {
  access_token: string
  token_type: string
  expires_in: number
}

export interface CadastroRequest {
  nomeCompleto: string
  email: string
  senha: string
  tipo: TipoUsuario
  telefone?: string
  cpf?: string
  apartamento?: string
  bloco?: string
}

export interface AtualizarUsuarioRequest {
  nomeCompleto?: string
  telefone?: string
  cpf?: string
  apartamento?: string
  bloco?: string
}

// Encomendas
export enum StatusEncomenda {
  RECEBIDA = 'RECEBIDA',
  RETIRADA = 'RETIRADA'
}

export interface Encomenda extends BaseEntity {
  nomeDestinatario: string
  apartamento: string
  bloco: string
  descricao: string
  recebidoPor?: string
  status: StatusEncomenda
  dataRecebimento: string
  dataRetirada?: string | null
  retiradoPorNome?: string | null
}

export interface ReceberEncomendaRequest {
  nomeDestinatario: string
  apartamento: string
  bloco: string
  descricao: string
}

export interface RetirarEncomendaRequest {
  retiradoPorNome: string
}

export interface RetiradaEncomendaResponse {
  encomendaId: number
  status: StatusEncomenda
  dataRetirada: string
  retiradoPorNome: string
}

export interface ListarEncomendasResponse {
  encomendas: Encomenda[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ListarEncomendasRequest {
  apartamento?: string
  bloco?: string
  data?: string
  page?: number
  size?: number
}

// Notificações
export enum StatusNotificacao {
  PENDENTE = 'PENDENTE',
  ENVIADA = 'ENVIADA',
  CONFIRMADA = 'CONFIRMADA'
}

export enum CanalNotificacao {
  EMAIL = 'EMAIL',
  SMS = 'SMS',
  WHATSAPP = 'WHATSAPP'
}

export interface Notificacao extends BaseEntity {
  encomendaId?: string
  canal: CanalNotificacao
  destino: string
  mensagem: string
  status: StatusNotificacao
  criadaEm: string
  enviadaEm?: string
  confirmadaEm?: string
}

export interface ListarNotificacoesResponse {
  notificacoes: Notificacao[]
  page: number
  size: number
}

// API Response
export interface ApiResponse<T = any> {
  data?: T
  error?: string
  message?: string
  status: number
}

// Form Types
export interface LoginForm {
  email: string
  password: string
}

export interface CadastroForm {
  nomeCompleto: string
  email: string
  senha: string
  confirmarSenha: string
  tipo: TipoUsuario
  telefone?: string
  cpf?: string
  apartamento?: string
  bloco?: string
}

export interface MeusDadosForm {
  nomeCompleto: string
  telefone?: string
  cpf?: string
  apartamento?: string
  bloco?: string
}

export interface ReceberEncomendaForm {
  nomeDestinatario: string
  apartamento: string
  bloco: string
  descricao: string
}

export interface RetirarEncomendaForm {
  retiradoPorNome: string
}
