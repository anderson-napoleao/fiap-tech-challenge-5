import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { Header } from '@/components/layout/Header'
import { Button } from '@/components/ui/Button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card'
import { authService } from '@/services/authService'
import { CadastroForm, TipoUsuario } from '@/types'

interface SignUpPageProps {
  onSignUpSuccess?: (userData: any) => void
}

export function SignUpPage({ onSignUpSuccess }: SignUpPageProps) {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string>('')
  const [success, setSuccess] = useState(false)
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<CadastroForm>()

  const senha = watch('senha')
  const tipoUsuarioSelecionado = watch('tipo')

  useEffect(() => {
    if (tipoUsuarioSelecionado === TipoUsuario.FUNCIONARIO) {
      setValue('apartamento', '')
      setValue('bloco', '')
    }
  }, [tipoUsuarioSelecionado, setValue])

  const onSubmit = async (data: CadastroForm) => {
    setIsLoading(true)
    setError('')

    try {
      await authService.cadastrar({
        nomeCompleto: data.nomeCompleto,
        email: data.email,
        senha: data.senha,
        tipo: data.tipo,
        telefone: data.telefone,
        cpf: data.cpf,
        apartamento: data.tipo === TipoUsuario.MORADOR ? data.apartamento : undefined,
        bloco: data.tipo === TipoUsuario.MORADOR ? data.bloco : undefined,
      })

      setSuccess(true)
      
      // Chamar callback de sucesso
      if (onSignUpSuccess) {
        onSignUpSuccess({ email: data.email })
      }

      // Redirecionar para login após 3 segundos
      setTimeout(() => {
        navigate('/login')
      }, 3000)

    } catch (err: any) {
      setError(err.message || 'Erro ao fazer cadastro')
    } finally {
      setIsLoading(false)
    }
  }

  if (success) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header mostrarLinksExternos={true} />
        
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="max-w-md mx-auto">
            <Card>
              <CardHeader className="text-center">
                <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100 mb-4">
                  <svg className="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <CardTitle className="text-2xl font-bold text-gray-900">
                  Cadastro Realizado!
                </CardTitle>
                <CardDescription>
                  Sua conta foi criada com sucesso. Você será redirecionado para a página de login.
                </CardDescription>
              </CardHeader>
              
              <CardContent className="text-center">
                <p className="text-sm text-gray-600 mb-4">
                  Redirecionando em <span className="font-medium">3</span> segundos...
                </p>
                <Link
                  to="/login"
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                >
                  Ir para Login Agora
                </Link>
              </CardContent>
            </Card>
          </div>
        </main>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header mostrarLinksExternos={true} />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="max-w-2xl mx-auto">
          <Card>
            <CardHeader className="text-center">
              <CardTitle className="text-2xl font-bold text-gray-900">
                Criar Nova Conta
              </CardTitle>
              <CardDescription>
                Preencha os dados abaixo para criar sua conta no sistema condominial
              </CardDescription>
            </CardHeader>
            
            <CardContent>
              <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                {error && (
                  <div className="bg-red-50 border border-red-200 rounded-md p-4">
                    <div className="flex">
                      <div className="ml-3">
                        <h3 className="text-sm font-medium text-red-800">
                          Erro no cadastro
                        </h3>
                        <div className="mt-2 text-sm text-red-700">
                          {error}
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Dados Pessoais */}
                <div className="space-y-4">
                  <h3 className="text-lg font-medium text-gray-900">Dados Pessoais</h3>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label htmlFor="nomeCompleto" className="block text-sm font-medium text-gray-700">
                        Nome Completo *
                      </label>
                      <input
                        {...register('nomeCompleto', {
                          required: 'Nome completo é obrigatório',
                          minLength: {
                            value: 3,
                            message: 'Nome deve ter pelo menos 3 caracteres',
                          },
                        })}
                        type="text"
                        id="nomeCompleto"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                        placeholder="João Silva"
                      />
                      {errors.nomeCompleto && (
                        <p className="mt-1 text-sm text-red-600">
                          {errors.nomeCompleto.message}
                        </p>
                      )}
                    </div>

                    <div>
                      <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                        Email *
                      </label>
                      <input
                        {...register('email', {
                          required: 'Email é obrigatório',
                          pattern: {
                            value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                            message: 'Email inválido',
                          },
                        })}
                        type="email"
                        id="email"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                        placeholder="joao@exemplo.com"
                      />
                      {errors.email && (
                        <p className="mt-1 text-sm text-red-600">
                          {errors.email.message}
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label htmlFor="senha" className="block text-sm font-medium text-gray-700">
                        Senha *
                      </label>
                      <input
                        {...register('senha', {
                          required: 'Senha é obrigatória',
                          minLength: {
                            value: 6,
                            message: 'Senha deve ter pelo menos 6 caracteres',
                          },
                        })}
                        type="password"
                        id="senha"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                        placeholder="••••••••"
                      />
                      {errors.senha && (
                        <p className="mt-1 text-sm text-red-600">
                          {errors.senha.message}
                        </p>
                      )}
                    </div>

                    <div>
                      <label htmlFor="confirmarSenha" className="block text-sm font-medium text-gray-700">
                        Confirmar Senha *
                      </label>
                      <input
                        {...register('confirmarSenha', {
                          required: 'Confirmação de senha é obrigatória',
                          validate: (value) => value === senha || 'Senhas não coincidem',
                        })}
                        type="password"
                        id="confirmarSenha"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                        placeholder="••••••••"
                      />
                      {errors.confirmarSenha && (
                        <p className="mt-1 text-sm text-red-600">
                          {errors.confirmarSenha.message}
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label htmlFor="telefone" className="block text-sm font-medium text-gray-700">
                        Telefone
                      </label>
                      <input
                        {...register('telefone', {
                          pattern: {
                            value: /^\d{10,11}$/,
                            message: 'Telefone deve ter 10 ou 11 dígitos',
                          },
                        })}
                        type="tel"
                        id="telefone"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                        placeholder="11999999999"
                      />
                      {errors.telefone && (
                        <p className="mt-1 text-sm text-red-600">
                          {errors.telefone.message}
                        </p>
                      )}
                    </div>

                    <div>
                      <label htmlFor="cpf" className="block text-sm font-medium text-gray-700">
                        CPF
                      </label>
                      <input
                        {...register('cpf', {
                          pattern: {
                            value: /^\d{11}$/,
                            message: 'CPF deve ter 11 dígitos',
                          },
                        })}
                        type="text"
                        id="cpf"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                        placeholder="12345678900"
                      />
                      {errors.cpf && (
                        <p className="mt-1 text-sm text-red-600">
                          {errors.cpf.message}
                        </p>
                      )}
                    </div>
                  </div>
                </div>

                {/* Tipo de Usuário */}
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Tipo de Usuário *
                  </label>
                  <div className="mt-2 space-y-2">
                    <label className="flex items-center">
                      <input
                        {...register('tipo', { required: 'Tipo de usuário é obrigatório' })}
                        type="radio"
                        value={TipoUsuario.MORADOR}
                        className="focus:ring-primary-500 h-4 w-4 text-primary-600 border-gray-300"
                      />
                      <span className="ml-2 text-sm text-gray-700">Morador</span>
                    </label>
                    <label className="flex items-center">
                      <input
                        {...register('tipo', { required: 'Tipo de usuário é obrigatório' })}
                        type="radio"
                        value={TipoUsuario.FUNCIONARIO}
                        className="focus:ring-primary-500 h-4 w-4 text-primary-600 border-gray-300"
                      />
                      <span className="ml-2 text-sm text-gray-700">Funcionário</span>
                    </label>
                  </div>
                  {errors.tipo && (
                    <p className="mt-1 text-sm text-red-600">
                      {errors.tipo.message}
                    </p>
                  )}
                </div>

                {tipoUsuarioSelecionado === TipoUsuario.MORADOR && (
                  <div id="dados-residenciais" className="space-y-4">
                    <h3 className="text-lg font-medium text-gray-900">Dados Residenciais</h3>
                    <p className="text-sm text-gray-500">Apenas para moradores</p>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <label htmlFor="apartamento" className="block text-sm font-medium text-gray-700">
                          Apartamento
                        </label>
                        <input
                          {...register('apartamento')}
                          type="text"
                          id="apartamento"
                          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                          placeholder="101"
                        />
                      </div>

                      <div>
                        <label htmlFor="bloco" className="block text-sm font-medium text-gray-700">
                          Bloco
                        </label>
                        <input
                          {...register('bloco')}
                          type="text"
                          id="bloco"
                          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                          placeholder="A"
                        />
                      </div>
                    </div>
                  </div>
                )}

                <div>
                  <Button
                    type="submit"
                    variant="primary"
                    size="lg"
                    disabled={isLoading}
                    className="w-full"
                  >
                    {isLoading ? 'Cadastrando...' : 'Criar Conta'}
                  </Button>
                </div>
              </form>

              <div className="mt-6 text-center">
                <p className="text-sm text-gray-600">
                  Já tem uma conta?{' '}
                  <Link
                    to="/login"
                    className="font-medium text-primary-600 hover:text-primary-500"
                  >
                    Faça login
                  </Link>
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  )
}
