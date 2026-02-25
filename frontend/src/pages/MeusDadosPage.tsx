import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { Header } from '@/components/layout/Header'
import { Button } from '@/components/ui/Button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card'
import { usuarioService } from '@/services/usuarioService'
import { Usuario, MeusDadosForm, TipoUsuario } from '@/types'
import { ArrowLeft, Save, User } from 'lucide-react'

export function MeusDadosPage() {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState<string>('')
  const [success, setSuccess] = useState(false)
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<MeusDadosForm>()

  useEffect(() => {
    carregarDadosUsuario()
  }, [])

  const carregarDadosUsuario = async () => {
    try {
      setIsLoading(true)
      const dadosUsuario = await usuarioService.getMeuPerfil()
      setUsuario(dadosUsuario)
      
      // Preencher formulário com dados atuais
      reset({
        nomeCompleto: dadosUsuario.nomeCompleto,
        telefone: dadosUsuario.telefone || '',
        cpf: dadosUsuario.cpf || '',
        apartamento: dadosUsuario.apartamento || '',
        bloco: dadosUsuario.bloco || '',
      })
    } catch (err: any) {
      setError(err.message || 'Erro ao carregar dados do usuário')
    } finally {
      setIsLoading(false)
    }
  }

  const onSubmit = async (data: MeusDadosForm) => {
    setIsSaving(true)
    setError('')
    setSuccess(false)

    try {
      // Para funcionários, não enviar dados residenciais
      const dadosAtualizacao = { ...data }
      if (usuario?.tipo === TipoUsuario.FUNCIONARIO) {
        delete dadosAtualizacao.apartamento
        delete dadosAtualizacao.bloco
      }

      await usuarioService.atualizarMeuPerfil(dadosAtualizacao)
      
      setSuccess(true)
      // Recarregar dados atualizados
      await carregarDadosUsuario()
      
      // Limpar sucesso após 3 segundos
      setTimeout(() => setSuccess(false), 3000)
    } catch (err: any) {
      setError(err.message || 'Erro ao atualizar dados')
    } finally {
      setIsSaving(false)
    }
  }

  const handleVoltar = () => {
    navigate('/dashboard')
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header 
          usuarioLogado={true}
          nomeUsuario={usuario?.nomeCompleto || ''}
          mostrarLinksExternos={true}
        />
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
            <p className="mt-4 text-gray-600">Carregando dados...</p>
          </div>
        </main>
      </div>
    )
  }

  if (!usuario) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header mostrarLinksExternos={true} />
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="text-center">
            <p className="text-red-600">Não foi possível carregar os dados do usuário.</p>
            <Button onClick={handleVoltar} className="mt-4">
              Voltar
            </Button>
          </div>
        </main>
      </div>
    )
  }

  const isFuncionario = usuario.tipo === TipoUsuario.FUNCIONARIO

  return (
    <div className="min-h-screen bg-gray-50">
      <Header 
        usuarioLogado={true}
        nomeUsuario={usuario.nomeCompleto}
        mostrarLinksExternos={true}
      />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="max-w-2xl mx-auto">
          {/* Header */}
          <div className="mb-8">
            <Button
              variant="ghost"
              onClick={handleVoltar}
              className="mb-4"
            >
              <ArrowLeft className="w-4 h-4 mr-2" />
              Voltar
            </Button>
            <div className="flex items-center space-x-3">
              <div className="p-3 bg-primary-100 rounded-lg">
                <User className="h-6 w-6 text-primary-600" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">
                  Meus Dados
                </h1>
                <p className="text-gray-600">
                  Atualize suas informações cadastrais
                </p>
              </div>
            </div>
          </div>

          <form onSubmit={handleSubmit(onSubmit)}>
            {/* Success Message */}
            {success && (
              <div className="bg-green-50 border border-green-200 rounded-md p-4 mb-6">
                <div className="flex">
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-green-800">
                      Dados atualizados com sucesso!
                    </h3>
                  </div>
                </div>
              </div>
            )}

            {/* Error Message */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
                <div className="flex">
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-red-800">
                      Erro ao atualizar dados
                    </h3>
                    <div className="mt-2 text-sm text-red-700">
                      {error}
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Dados Pessoais */}
            <Card className="mb-6">
              <CardHeader>
                <CardTitle className="text-lg">Dados Pessoais</CardTitle>
                <CardDescription>
                  Informações básicas do seu cadastro
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Nome Completo
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
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                    placeholder="Seu nome completo"
                  />
                  {errors.nomeCompleto && (
                    <p className="mt-1 text-sm text-red-600">
                      {errors.nomeCompleto.message}
                    </p>
                  )}
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Email
                    </label>
                    <input
                      type="email"
                      value={usuario.email}
                      disabled
                      className="mt-1 block w-full rounded-md border-gray-300 bg-gray-50 shadow-sm sm:text-sm"
                    />
                    <p className="mt-1 text-xs text-gray-500">
                      Email não pode ser alterado
                    </p>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Tipo de Usuário
                    </label>
                    <input
                      type="text"
                      value={usuario.tipo}
                      disabled
                      className="mt-1 block w-full rounded-md border-gray-300 bg-gray-50 shadow-sm sm:text-sm"
                    />
                    <p className="mt-1 text-xs text-gray-500">
                      Tipo não pode ser alterado
                    </p>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
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
                    <label className="block text-sm font-medium text-gray-700">
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
              </CardContent>
            </Card>

            {/* Dados Residenciais (apenas para moradores) */}
            {!isFuncionario && (
              <Card className="mb-6">
                <CardHeader>
                  <CardTitle className="text-lg">Dados Residenciais</CardTitle>
                  <CardDescription>
                    Informações do seu apartamento
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        Apartamento
                      </label>
                      <input
                        {...register('apartamento')}
                        type="text"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                        placeholder="101"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        Bloco
                      </label>
                      <input
                        {...register('bloco')}
                        type="text"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                        placeholder="A"
                      />
                    </div>
                  </div>
                </CardContent>
              </Card>
            )}

            {/* Botões de Ação */}
            <div className="flex justify-end space-x-4">
              <Button
                type="button"
                variant="outline"
                onClick={handleVoltar}
                disabled={isSaving}
              >
                Cancelar
              </Button>
              <Button
                type="submit"
                variant="primary"
                disabled={isSaving || !isDirty}
              >
                <Save className="w-4 h-4 mr-2" />
                {isSaving ? 'Salvando...' : 'Salvar Alterações'}
              </Button>
            </div>
          </form>
        </div>
      </main>
    </div>
  )
}
