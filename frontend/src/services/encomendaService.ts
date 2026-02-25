import axios, { AxiosInstance } from 'axios'
import {
  Encomenda,
  ListarEncomendasRequest,
  ListarEncomendasResponse,
  ReceberEncomendaRequest,
  RetirarEncomendaRequest,
  RetiradaEncomendaResponse,
} from '@/types'

class EncomendaService {
  private api: AxiosInstance

  constructor() {
    this.api = axios.create({
      baseURL: import.meta.env.VITE_API_ENCOMENDA_URL || 'http://localhost:8083',
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

  async receberEncomenda(data: ReceberEncomendaRequest): Promise<Encomenda> {
    try {
      const response = await this.api.post('/portaria/encomendas', data)
      return response.data
    } catch (error: any) {
      if (error.response?.status === 400) {
        throw new Error('Dados inválidos para receber encomenda.')
      }
      if (error.response?.status === 401) {
        throw new Error('Não autorizado. Faça login novamente.')
      }
      if (error.response?.status === 403) {
        throw new Error('Acesso negado. Esta operação é permitida apenas para funcionário.')
      }
      throw new Error('Erro ao registrar recebimento de encomenda.')
    }
  }

  async retirarEncomenda(id: number, data: RetirarEncomendaRequest): Promise<RetiradaEncomendaResponse> {
    try {
      const response = await this.api.post(`/portaria/encomendas/${id}/retirada`, data)
      return response.data
    } catch (error: any) {
      if (error.response?.status === 404) {
        throw new Error('Encomenda não encontrada.')
      }
      if (error.response?.status === 400) {
        throw new Error('Dados inválidos para retirada de encomenda.')
      }
      if (error.response?.status === 401) {
        throw new Error('Não autorizado. Faça login novamente.')
      }
      if (error.response?.status === 403) {
        throw new Error('Acesso negado. Esta operação é permitida apenas para funcionário.')
      }
      throw new Error('Erro ao registrar retirada de encomenda.')
    }
  }

  async buscarEncomendaPorId(id: number): Promise<Encomenda> {
    try {
      const response = await this.api.get(`/portaria/encomendas/${id}`)
      return response.data
    } catch (error: any) {
      if (error.response?.status === 404) {
        throw new Error('Encomenda não encontrada.')
      }
      if (error.response?.status === 401) {
        throw new Error('Não autorizado. Faça login novamente.')
      }
      if (error.response?.status === 403) {
        throw new Error('Acesso negado. Esta operação é permitida apenas para funcionário.')
      }
      throw new Error('Erro ao buscar encomenda.')
    }
  }

  async listarEncomendas(params: ListarEncomendasRequest): Promise<ListarEncomendasResponse> {
    try {
      const response = await this.api.get('/portaria/encomendas', { params })
      return response.data
    } catch (error: any) {
      if (error.response?.status === 400) {
        throw new Error('Filtros ou paginacao invalidos.')
      }
      if (error.response?.status === 401) {
        throw new Error('Nao autorizado. Faca login novamente.')
      }
      if (error.response?.status === 403) {
        throw new Error('Acesso negado. Esta operacao e permitida apenas para funcionario.')
      }
      throw new Error('Erro ao listar encomendas.')
    }
  }
}

export const encomendaService = new EncomendaService()
