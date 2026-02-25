import React, { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { Header } from '@/components/layout/Header'
import { Button } from '@/components/ui/Button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card'
import { encomendaService } from '@/services/encomendaService'
import { Encomenda, RetirarEncomendaForm, RetiradaEncomendaResponse, StatusEncomenda } from '@/types'
import { ArrowLeft, Package, CheckCircle, AlertCircle, Search } from 'lucide-react'

export function RetirarEncomendaPage() {
  const [isLoading, setIsLoading] = useState(false)
  const [searchingEncomenda, setSearchingEncomenda] = useState(false)
  const [encomendaId, setEncomendaId] = useState<string>('')
  const [encomendaEncontrada, setEncomendaEncontrada] = useState<Encomenda | null>(null)
  const [retiradaRealizada, setRetiradaRealizada] = useState<RetiradaEncomendaResponse | null>(null)
  const [error, setError] = useState<string>('')
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<RetirarEncomendaForm>()

  const buscarEncomenda = async () => {
    const idNormalizado = Number(encomendaId)

    if (!Number.isInteger(idNormalizado) || idNormalizado <= 0) {
      setError('Digite um ID de encomenda válido')
      return
    }

    setSearchingEncomenda(true)
    setError('')
    setEncomendaEncontrada(null)

    try {
      const encomenda = await encomendaService.buscarEncomendaPorId(idNormalizado)

      if (encomenda.status !== StatusEncomenda.RECEBIDA) {
        setError('Esta encomenda já foi retirada e não pode receber nova baixa.')
        return
      }

      setEncomendaEncontrada(encomenda)
    } catch (err: any) {
      setError(err.message || 'Encomenda não encontrada')
    } finally {
      setSearchingEncomenda(false)
    }
  }

  const onSubmit = async (data: RetirarEncomendaForm) => {
    if (!encomendaEncontrada) {
      setError('Busque uma encomenda primeiro')
      return
    }

    setIsLoading(true)
    setError('')
    setRetiradaRealizada(null)

    try {
      const retirada = await encomendaService.retirarEncomenda(Number(encomendaEncontrada.id), data)
      setRetiradaRealizada(retirada)
      reset()
      setEncomendaEncontrada(null)
      setEncomendaId('')
    } catch (err: any) {
      setError(err.message || 'Erro ao registrar retirada')
    } finally {
      setIsLoading(false)
    }
  }

  const handleVoltar = () => {
    navigate('/dashboard')
  }

  const handleNovaRetirada = () => {
    setRetiradaRealizada(null)
    setEncomendaEncontrada(null)
    setEncomendaId('')
    reset()
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      buscarEncomenda()
    }
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
              <div className="p-3 bg-green-100 rounded-lg">
                <Package className="h-6 w-6 text-green-600" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">
                  Retirar Encomenda
                </h1>
                <p className="text-gray-600">
                  Registre a retirada de encomendas pelos moradores
                </p>
              </div>
            </div>
          </div>

          {/* Success Message */}
          {retiradaRealizada && (
            <div className="bg-green-50 border border-green-200 rounded-md p-6 mb-6">
              <div className="flex">
                <div className="flex-shrink-0">
                  <CheckCircle className="h-5 w-5 text-green-400" />
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-green-800">
                    Retirada registrada com sucesso!
                  </h3>
                  <div className="mt-2 text-sm text-green-700">
                    <div className="bg-white rounded p-3 mt-2">
                      <p><strong>ID Encomenda:</strong> #{retiradaRealizada.encomendaId}</p>
                      <p><strong>Status:</strong> {retiradaRealizada.status}</p>
                      <p><strong>Retirada em:</strong> {new Date(retiradaRealizada.dataRetirada).toLocaleString('pt-BR')}</p>
                      <p><strong>Retirada por:</strong> {retiradaRealizada.retiradoPorNome}</p>
                    </div>
                  </div>
                  <div className="mt-4">
                    <Button
                      variant="primary"
                      onClick={handleNovaRetirada}
                      size="sm"
                    >
                      Registrar Nova Retirada
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
                    Erro
                  </h3>
                  <div className="mt-2 text-sm text-red-700">
                    {error}
                  </div>
                </div>
              </div>
            </div>
          )}

          {!retiradaRealizada && (
            <>
              {/* Busca de Encomenda */}
              <Card className="mb-6">
                <CardHeader>
                  <CardTitle className="text-lg">Buscar Encomenda</CardTitle>
                  <CardDescription>
                    Digite o ID da encomenda para registrar a retirada
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex space-x-4">
                    <div className="flex-1">
                      <input
                        type="text"
                        value={encomendaId}
                        onChange={(e) => setEncomendaId(e.target.value)}
                        onKeyPress={handleKeyPress}
                        placeholder="Digite o ID da encomenda (ex: 123)"
                        className="block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                      />
                    </div>
                    <Button
                      variant="outline"
                      onClick={buscarEncomenda}
                      disabled={searchingEncomenda}
                    >
                      <Search className="w-4 h-4 mr-2" />
                      {searchingEncomenda ? 'Buscando...' : 'Buscar'}
                    </Button>
                  </div>
                </CardContent>
              </Card>

              {/* Detalhes da Encomenda Encontrada */}
              {encomendaEncontrada && (
                <Card className="mb-6">
                  <CardHeader>
                    <CardTitle className="text-lg">Encomenda Encontrada</CardTitle>
                    <CardDescription>
                      Confirme os dados e registre a retirada
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="bg-gray-50 rounded-lg p-4 mb-4">
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                          <p className="text-sm font-medium text-gray-500">ID</p>
                          <p className="text-lg font-semibold">#{encomendaEncontrada.id}</p>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-500">Destinatário</p>
                          <p className="text-lg">{encomendaEncontrada.nomeDestinatario}</p>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-500">Apartamento</p>
                          <p className="text-lg">{encomendaEncontrada.apartamento} {encomendaEncontrada.bloco}</p>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-500">Status</p>
                          <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                            {encomendaEncontrada.status}
                          </span>
                        </div>
                        <div className="md:col-span-2">
                          <p className="text-sm font-medium text-gray-500">Descrição</p>
                          <p className="text-lg">{encomendaEncontrada.descricao}</p>
                        </div>
                      </div>
                    </div>

                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                      <div>
                        <label htmlFor="retiradoPorNome" className="block text-sm font-medium text-gray-700">
                          Nome de quem retirou *
                        </label>
                        <input
                          {...register('retiradoPorNome', {
                            required: 'Nome de quem retirou é obrigatório',
                            minLength: {
                              value: 3,
                              message: 'Nome deve ter pelo menos 3 caracteres',
                            },
                          })}
                          type="text"
                          id="retiradoPorNome"
                          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                          placeholder="João Silva"
                        />
                        {errors.retiradoPorNome && (
                          <p className="mt-1 text-sm text-red-600">
                            {errors.retiradoPorNome.message}
                          </p>
                        )}
                      </div>

                      <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4">
                        <div className="flex">
                          <div className="ml-3">
                            <h3 className="text-sm font-medium text-yellow-800">
                              ⚠️ Confirmação obrigatória
                            </h3>
                            <div className="mt-2 text-sm text-yellow-700">
                              Verifique se a pessoa que está retirando é realmente o destinatário 
                              ou possui autorização. A retirada será registrada permanentemente.
                            </div>
                          </div>
                        </div>
                      </div>

                      <div className="flex justify-end space-x-4">
                        <Button
                          type="button"
                          variant="outline"
                          onClick={() => {
                            setEncomendaEncontrada(null)
                            setEncomendaId('')
                          }}
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
                          {isLoading ? 'Registrando...' : 'Confirmar Retirada'}
                        </Button>
                      </div>
                    </form>
                  </CardContent>
                </Card>
              )}

              {/* Orientação de busca */}
              {!encomendaEncontrada && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Como testar esta funcionalidade</CardTitle>
                    <CardDescription>
                      A busca é feita no backend pelo ID real da encomenda.
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-gray-700">
                      Primeiro registre um recebimento em <strong>Receber Encomenda</strong>. Em seguida, use o ID
                      retornado para buscar aqui e confirmar a retirada.
                    </p>
                  </CardContent>
                </Card>
              )}
            </>
          )}
        </div>
      </main>
    </div>
  )
}
