import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { Header } from '@/components/layout/Header'
import { Button } from '@/components/ui/Button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card'
import { encomendaService } from '@/services/encomendaService'
import { ReceberEncomendaForm, Encomenda } from '@/types'
import { ArrowLeft, Package, CheckCircle, AlertCircle } from 'lucide-react'

export function ReceberEncomendaPage() {
  const [isLoading, setIsLoading] = useState(false)
  const [encomendaRecebida, setEncomendaRecebida] = useState<Encomenda | null>(null)
  const [error, setError] = useState<string>('')
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ReceberEncomendaForm>()

  const onSubmit = async (data: ReceberEncomendaForm) => {
    setIsLoading(true)
    setError('')
    setEncomendaRecebida(null)

    try {
      const encomenda = await encomendaService.receberEncomenda(data)
      setEncomendaRecebida(encomenda)
      reset() // Limpar formulário após sucesso
    } catch (err: any) {
      setError(err.message || 'Erro ao registrar encomenda')
    } finally {
      setIsLoading(false)
    }
  }

  const handleVoltar = () => {
    navigate('/dashboard')
  }

  const handleNovaEncomenda = () => {
    setEncomendaRecebida(null)
    reset()
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header 
        usuarioLogado={true}
        nomeUsuario=""
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
              <div className="p-3 bg-blue-100 rounded-lg">
                <Package className="h-6 w-6 text-blue-600" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">
                  Receber Encomenda
                </h1>
                <p className="text-gray-600">
                  Registre o recebimento de novas encomendas na portaria
                </p>
              </div>
            </div>
          </div>

          {/* Success Message */}
          {encomendaRecebida && (
            <div className="bg-green-50 border border-green-200 rounded-md p-6 mb-6">
              <div className="flex">
                <div className="flex-shrink-0">
                  <CheckCircle className="h-5 w-5 text-green-400" />
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-green-800">
                    Encomenda recebida com sucesso!
                  </h3>
                  <div className="mt-2 text-sm text-green-700">
                    <div className="bg-white rounded p-3 mt-2">
                      <p><strong>ID:</strong> #{encomendaRecebida.id}</p>
                      <p><strong>Destinatário:</strong> {encomendaRecebida.nomeDestinatario}</p>
                      <p><strong>Apartamento:</strong> {encomendaRecebida.apartamento} {encomendaRecebida.bloco}</p>
                      <p><strong>Descrição:</strong> {encomendaRecebida.descricao}</p>
                      <p><strong>Status:</strong> {encomendaRecebida.status}</p>
                      <p><strong>Recebido por:</strong> {encomendaRecebida.recebidoPor || 'Portaria'}</p>
                    </div>
                  </div>
                  <div className="mt-4">
                    <Button
                      variant="primary"
                      onClick={handleNovaEncomenda}
                      size="sm"
                    >
                      Receber Nova Encomenda
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Error Message */}
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
              <div className="flex">
                <div className="flex-shrink-0">
                  <AlertCircle className="h-5 w-5 text-red-400" />
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-red-800">
                    Erro ao registrar encomenda
                  </h3>
                  <div className="mt-2 text-sm text-red-700">
                    {error}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Formulário (ocultar após sucesso) */}
          {!encomendaRecebida && (
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Dados da Encomenda</CardTitle>
                <CardDescription>
                  Preencha as informações da encomenda recebida
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label htmlFor="nomeDestinatario" className="block text-sm font-medium text-gray-700">
                        Nome do Destinatário *
                      </label>
                      <input
                        {...register('nomeDestinatario', {
                          required: 'Nome do destinatário é obrigatório',
                          minLength: {
                            value: 3,
                            message: 'Nome deve ter pelo menos 3 caracteres',
                          },
                        })}
                        type="text"
                        id="nomeDestinatario"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                        placeholder="João Silva"
                      />
                      {errors.nomeDestinatario && (
                        <p className="mt-1 text-sm text-red-600">
                          {errors.nomeDestinatario.message}
                        </p>
                      )}
                    </div>

                    <div>
                      <label htmlFor="descricao" className="block text-sm font-medium text-gray-700">
                        Descrição *
                      </label>
                      <input
                        {...register('descricao', {
                          required: 'Descrição é obrigatória',
                          minLength: {
                            value: 3,
                            message: 'Descrição deve ter pelo menos 3 caracteres',
                          },
                        })}
                        type="text"
                        id="descricao"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                        placeholder="Caixa pequena - Amazon"
                      />
                      {errors.descricao && (
                        <p className="mt-1 text-sm text-red-600">
                          {errors.descricao.message}
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label htmlFor="apartamento" className="block text-sm font-medium text-gray-700">
                        Apartamento *
                      </label>
                      <input
                        {...register('apartamento', {
                          required: 'Apartamento é obrigatório',
                          pattern: {
                            value: /^[0-9A-Za-z]+$/,
                            message: 'Apenas números e letras',
                          },
                        })}
                        type="text"
                        id="apartamento"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                        placeholder="101"
                      />
                      {errors.apartamento && (
                        <p className="mt-1 text-sm text-red-600">
                          {errors.apartamento.message}
                        </p>
                      )}
                    </div>

                    <div>
                      <label htmlFor="bloco" className="block text-sm font-medium text-gray-700">
                        Bloco *
                      </label>
                      <input
                        {...register('bloco', {
                          required: 'Bloco é obrigatório',
                          pattern: {
                            value: /^[A-Za-z]$/,
                            message: 'Apenas uma letra (A, B, C, etc.)',
                          },
                        })}
                        type="text"
                        id="bloco"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                        placeholder="A"
                        maxLength={1}
                      />
                      {errors.bloco && (
                        <p className="mt-1 text-sm text-red-600">
                          {errors.bloco.message}
                        </p>
                      )}
                    </div>
                  </div>

                  {/* Informações de ajuda */}
                  <div className="bg-blue-50 border border-blue-200 rounded-md p-4">
                    <div className="flex">
                      <div className="ml-3">
                        <h3 className="text-sm font-medium text-blue-800">
                          Dicas de preenchimento
                        </h3>
                        <div className="mt-2 text-sm text-blue-700">
                          <ul className="list-disc list-inside space-y-1">
                            <li>Apartamento: use apenas números e letras (ex: 101, 201A)</li>
                            <li>Bloco: use uma única letra (A, B, C, etc.)</li>
                            <li>Descrição: seja específico (ex: "Caixa média - Mercado Livre")</li>
                          </ul>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Botões de Ação */}
                  <div className="flex justify-end space-x-4">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={handleVoltar}
                      disabled={isLoading}
                    >
                      Cancelar
                    </Button>
                    <Button
                      type="submit"
                      variant="primary"
                      disabled={isLoading}
                    >
                      <Package className="w-4 h-4 mr-2" />
                      {isLoading ? 'Registrando...' : 'Registrar Recebimento'}
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          )}

          {/* Estatísticas Rápidas */}
          <div className="mt-8">
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Estatísticas do Dia</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="text-center p-4 bg-blue-50 rounded-lg">
                    <div className="text-2xl font-bold text-blue-600">5</div>
                    <div className="text-sm text-gray-600">Encomendas Recebidas</div>
                  </div>
                  <div className="text-center p-4 bg-green-50 rounded-lg">
                    <div className="text-2xl font-bold text-green-600">3</div>
                    <div className="text-sm text-gray-600">Encomendas Retiradas</div>
                  </div>
                  <div className="text-center p-4 bg-orange-50 rounded-lg">
                    <div className="text-2xl font-bold text-orange-600">2</div>
                    <div className="text-sm text-gray-600">Pendentes de Retirada</div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
    </div>
  )
}
