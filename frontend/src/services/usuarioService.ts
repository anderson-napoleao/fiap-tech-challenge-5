import axios, { AxiosInstance } from 'axios'
import { Usuario, AtualizarUsuarioRequest } from '@/types'

class UsuarioService {
  private api: AxiosInstance

  constructor() {
    this.api = axios.create({
      baseURL: import.meta.env.VITE_API_USUARIO_URL || 'http://localhost:8082',
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
          // Token expirado ou inválido
          localStorage.removeItem('auth_token')
          localStorage.removeItem('user_data')
          window.location.href = '/'
        }
        return Promise.reject(error)
      }
    )
  }

  async getMeuPerfil(): Promise<Usuario> {
    try {
      const response = await this.api.get('/users/me')
      return response.data
    } catch (error: any) {
      if (error.response?.status === 401) {
        throw new Error('Não autorizado. Faça login novamente.')
      }
      if (error.response?.status === 404) {
        throw new Error('Usuário não encontrado.')
      }
      throw new Error('Erro ao buscar dados do usuário.')
    }
  }

  async atualizarMeuPerfil(data: AtualizarUsuarioRequest): Promise<Usuario> {
    try {
      const response = await this.api.put('/users/me', data)
      return response.data
    } catch (error: any) {
      if (error.response?.status === 401) {
        throw new Error('Não autorizado. Faça login novamente.')
      }
      if (error.response?.status === 400) {
        throw new Error('Dados inválidos para atualização.')
      }
      throw new Error('Erro ao atualizar dados do usuário.')
    }
  }
}

export const usuarioService = new UsuarioService()
