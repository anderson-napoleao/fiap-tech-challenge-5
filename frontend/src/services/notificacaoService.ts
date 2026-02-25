import axios, { AxiosInstance } from 'axios'
import { ListarNotificacoesResponse } from '@/types'

class NotificacaoService {
  private api: AxiosInstance

  constructor() {
    this.api = axios.create({
      baseURL: import.meta.env.VITE_API_NOTIFICACAO_URL || 'http://localhost:8087',
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    })

    // Interceptor para adicionar token JWT
    this.api.interceptors.request.use((config) => {
      const token = localStorage.getItem('auth_token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    })

    // Interceptor para tratar erros 401
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('auth_token')
          localStorage.removeItem('user_data')
          window.location.href = '/'
        }
        return Promise.reject(error)
      }
    )
  }

  async listarNotificacoesPendentes(
    page: number = 0,
    size: number = 20,
    confirmada: boolean = false
  ): Promise<ListarNotificacoesResponse> {
    try {
      const response = await this.api.get('/morador/notificacoes', {
        params: { confirmada, page, size }
      })
      return response.data
    } catch (error: any) {
      if (error.response?.status === 401) {
        throw new Error('Não autorizado. Faça login novamente.')
      }
      if (error.response?.status === 403) {
        throw new Error('Acesso negado. Esta operação é permitida apenas para morador.')
      }
      throw new Error('Erro ao listar notificações.')
    }
  }

  async confirmarNotificacao(notificacaoId: string): Promise<void> {
    try {
      await this.api.post(`/morador/notificacoes/${notificacaoId}/confirmacao`)
    } catch (error: any) {
      if (error.response?.status === 404) {
        throw new Error('Notificação não encontrada.')
      }
      if (error.response?.status === 401) {
        throw new Error('Não autorizado. Faça login novamente.')
      }
      if (error.response?.status === 403) {
        throw new Error('Acesso negado. Esta operação é permitida apenas para morador.')
      }
      if (error.response?.status === 400) {
        throw new Error('Não é possível confirmar esta notificação.')
      }
      throw new Error('Erro ao confirmar notificação.')
    }
  }

  // Método para confirmar múltiplas notificações em lote
  async confirmarNotificacoesLote(notificacoesIds: string[]): Promise<{ sucesso: string[], falhas: { id: string, erro: string }[] }> {
    const sucesso: string[] = []
    const falhas: { id: string, erro: string }[] = []

    for (const id of notificacoesIds) {
      try {
        await this.confirmarNotificacao(id)
        sucesso.push(id)
      } catch (error: any) {
        falhas.push({ id, erro: error.message })
      }
    }

    return { sucesso, falhas }
  }
}

export const notificacaoService = new NotificacaoService()
