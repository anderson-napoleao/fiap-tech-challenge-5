import axios, { AxiosInstance, AxiosResponse } from 'axios'
import { LoginRequest, LoginResponse, CadastroRequest, Usuario } from '@/types'

class AuthService {
  private api: AxiosInstance

  constructor() {
    this.api = axios.create({
      baseURL: import.meta.env.VITE_API_IDENTITY_URL || 'http://localhost:8081',
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    })
  }

  async login(credentials: LoginRequest): Promise<LoginResponse> {
    try {
      const response: AxiosResponse<LoginResponse> = await this.api.post('/auth/token', credentials)
      return response.data
    } catch (error: any) {
      if (error.response?.status === 401) {
        throw new Error('Email ou senha inválidos')
      }
      if (error.response?.status === 400) {
        throw new Error('Dados de login inválidos')
      }
      throw new Error('Erro ao fazer login. Tente novamente.')
    }
  }

  async cadastrar(userData: CadastroRequest): Promise<Usuario> {
    try {
      // Cadastro é feito no serviço de usuário, não identidade
      const response = await axios.post(`${import.meta.env.VITE_API_USUARIO_URL || 'http://localhost:8082'}/users`, userData)
      return response.data
    } catch (error: any) {
      if (error.response?.status === 409) {
        throw new Error('Email já cadastrado')
      }
      if (error.response?.status === 400) {
        throw new Error('Dados de cadastro inválidos')
      }
      throw new Error('Erro ao fazer cadastro. Tente novamente.')
    }
  }

  async validateToken(token: string): Promise<any> {
    const decoded = this.decodeToken(token)
    if (!decoded || this.isTokenExpired(token)) {
      throw new Error('Token inválido')
    }
    return decoded
  }

  // Decodifica JWT sem validação (para obter dados básicos)
  decodeToken(token: string): any {
    try {
      const base64Url = token.split('.')[1]
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
      return JSON.parse(window.atob(base64))
    } catch (error) {
      return null
    }
  }

  // Verifica se token está expirado
  isTokenExpired(token: string): boolean {
    const decoded = this.decodeToken(token)
    if (!decoded || !decoded.exp) {
      return true
    }
    const currentTime = Date.now() / 1000
    return decoded.exp < currentTime
  }

  // Obtém ID do usuário do token
  getUserIdFromToken(token: string): string | null {
    const decoded = this.decodeToken(token)
    return decoded?.sub || null
  }

  // Obtém email do usuário do token
  getEmailFromToken(token: string): string | null {
    const decoded = this.decodeToken(token)
    return decoded?.sub || null
  }
}

export const authService = new AuthService()
