import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { Header } from '@/components/layout/Header'
import { Button } from '@/components/ui/Button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card'
import { encomendaService } from '@/services/encomendaService'
import { Encomenda, RetirarEncomendaForm, RetiradaEncomendaResponse, StatusEncomenda } from '@/types'
import { ArrowLeft, Package, CheckCircle, AlertCircle, Search } from 'lucide-react'

const PAGE_SIZE = 10

export function RetirarEncomendaPage() {
  const [isLoadingRetirada, setIsLoadingRetirada] = useState(false)
  const [isLoadingLista, setIsLoadingLista] = useState(false)
  const [listaEncomendas, setListaEncomendas] = useState<Encomenda[]>([])
  const [encomendaSelecionada, setEncomendaSelecionada] = useState<Encomenda | null>(null)
  const [retiradaRealizada, setRetiradaRealizada] = useState<RetiradaEncomendaResponse | null>(null)
  const [error, setError] = useState('')

  const [filtroApartamento, setFiltroApartamento] = useState('')
  const [filtroBloco, setFiltroBloco] = useState('')
  const [aplicadoApartamento, setAplicadoApartamento] = useState('')
  const [aplicadoBloco, setAplicadoBloco] = useState('')

  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<RetirarEncomendaForm>()

  const carregarEncomendas = async (page: number, apartamento: string, bloco: string) => {
    setIsLoadingLista(true)
    setError('')

    try {
      const response = await encomendaService.listarEncomendas({
        page,
        size: PAGE_SIZE,
        apartamento: apartamento || undefined,
        bloco: bloco || undefined,
      })

      if (response.totalPages > 0 && page >= response.totalPages) {
        setCurrentPage(response.totalPages - 1)
        return
      }

      setListaEncomendas(response.encomendas)
      setTotalPages(response.totalPages)
      setTotalElements(response.totalElements)

      if (encomendaSelecionada) {
        const atualizada = response.encomendas.find((item) => Number(item.id) === Number(encomendaSelecionada.id))
        setEncomendaSelecionada(atualizada || null)
      }
    } catch (err: any) {
      setError(err.message || 'Erro ao carregar encomendas')
    } finally {
      setIsLoadingLista(false)
    }
  }

  useEffect(() => {
    carregarEncomendas(currentPage, aplicadoApartamento, aplicadoBloco)
  }, [currentPage, aplicadoApartamento, aplicadoBloco])

  const onSubmit = async (data: RetirarEncomendaForm) => {
    if (!encomendaSelecionada) {
      setError('Selecione uma encomenda para registrar a retirada')
      return
    }

    if (encomendaSelecionada.status !== StatusEncomenda.RECEBIDA) {
      setError('Somente encomendas com status RECEBIDA podem ser retiradas')
      return
    }

    setIsLoadingRetirada(true)
    setError('')
    setRetiradaRealizada(null)

    try {
      const retirada = await encomendaService.retirarEncomenda(Number(encomendaSelecionada.id), data)
      setRetiradaRealizada(retirada)
      setEncomendaSelecionada(null)
      reset()
      await carregarEncomendas(currentPage, aplicadoApartamento, aplicadoBloco)
    } catch (err: any) {
      setError(err.message || 'Erro ao registrar retirada')
    } finally {
      setIsLoadingRetirada(false)
    }
  }

  const handleVoltar = () => {
    navigate('/dashboard')
  }

  const handleNovaRetirada = async () => {
    setRetiradaRealizada(null)
    setEncomendaSelecionada(null)
    reset()
    await carregarEncomendas(currentPage, aplicadoApartamento, aplicadoBloco)
  }

  const aplicarFiltros = () => {
    const apartamento = filtroApartamento.trim()
    const bloco = filtroBloco.trim()
    setCurrentPage(0)
    setAplicadoApartamento(apartamento)
    setAplicadoBloco(bloco)
    setEncomendaSelecionada(null)
  }

  const limparFiltros = () => {
    setFiltroApartamento('')
    setFiltroBloco('')
    setCurrentPage(0)
    setAplicadoApartamento('')
    setAplicadoBloco('')
    setEncomendaSelecionada(null)
  }

  const selecionarEncomenda = (item: Encomenda) => {
    if (item.status !== StatusEncomenda.RECEBIDA) {
      setError('Essa encomenda ja esta retirada e nao pode receber nova baixa')
      return
    }

    setError('')
    setEncomendaSelecionada(item)
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header usuarioLogado={true} nomeUsuario="" mostrarLinksExternos={true} />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="max-w-5xl mx-auto">
          <div className="mb-8">
            <Button variant="ghost" onClick={handleVoltar} className="mb-4">
              <ArrowLeft className="w-4 h-4 mr-2" />
              Voltar
            </Button>
            <div className="flex items-center space-x-3">
              <div className="p-3 bg-green-100 rounded-lg">
                <Package className="h-6 w-6 text-green-600" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">Retirar Encomenda</h1>
                <p className="text-gray-600">
                  Liste as encomendas, filtre por unidade e registre a retirada.
                </p>
              </div>
            </div>
          </div>

          {retiradaRealizada && (
            <div className="bg-green-50 border border-green-200 rounded-md p-6 mb-6">
              <div className="flex">
                <div className="flex-shrink-0">
                  <CheckCircle className="h-5 w-5 text-green-400" />
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-green-800">Retirada registrada com sucesso</h3>
                  <div className="mt-2 text-sm text-green-700">
                    <div className="bg-white rounded p-3 mt-2">
                      <p>
                        <strong>ID Encomenda:</strong> #{retiradaRealizada.encomendaId}
                      </p>
                      <p>
                        <strong>Status:</strong> {retiradaRealizada.status}
                      </p>
                      <p>
                        <strong>Retirada em:</strong>{' '}
                        {new Date(retiradaRealizada.dataRetirada).toLocaleString('pt-BR')}
                      </p>
                      <p>
                        <strong>Retirada por:</strong> {retiradaRealizada.retiradoPorNome}
                      </p>
                    </div>
                  </div>
                  <div className="mt-4">
                    <Button variant="primary" onClick={handleNovaRetirada} size="sm">
                      Registrar Nova Retirada
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          )}

          {error && (
            <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
              <div className="flex">
                <div className="flex-shrink-0">
                  <AlertCircle className="h-5 w-5 text-red-400" />
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-red-800">Erro</h3>
                  <div className="mt-2 text-sm text-red-700">{error}</div>
                </div>
              </div>
            </div>
          )}

          {!retiradaRealizada && (
            <>
              <Card className="mb-6">
                <CardHeader>
                  <CardTitle className="text-lg">Filtros de busca</CardTitle>
                  <CardDescription>
                    Filtre por apartamento e bloco. A lista e paginada em {PAGE_SIZE} itens.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
                    <div>
                      <label htmlFor="filtro-apartamento" className="block text-sm font-medium text-gray-700">
                        Apartamento
                      </label>
                      <input
                        id="filtro-apartamento"
                        value={filtroApartamento}
                        onChange={(event) => setFiltroApartamento(event.target.value)}
                        placeholder="Ex: 101"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                      />
                    </div>
                    <div>
                      <label htmlFor="filtro-bloco" className="block text-sm font-medium text-gray-700">
                        Bloco
                      </label>
                      <input
                        id="filtro-bloco"
                        value={filtroBloco}
                        onChange={(event) => setFiltroBloco(event.target.value)}
                        placeholder="Ex: A"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                      />
                    </div>
                    <Button type="button" variant="outline" onClick={aplicarFiltros}>
                      <Search className="w-4 h-4 mr-2" />
                      Aplicar
                    </Button>
                    <Button type="button" variant="ghost" onClick={limparFiltros}>
                      Limpar
                    </Button>
                  </div>
                </CardContent>
              </Card>

              <Card className="mb-6">
                <CardHeader>
                  <CardTitle className="text-lg">Encomendas para retirada</CardTitle>
                  <CardDescription>
                    Selecione uma encomenda com status RECEBIDA para abrir os detalhes.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {isLoadingLista ? (
                    <p className="text-sm text-gray-600">Carregando encomendas...</p>
                  ) : listaEncomendas.length === 0 ? (
                    <p className="text-sm text-gray-600">
                      Nenhuma encomenda encontrada para os filtros informados.
                    </p>
                  ) : (
                    <div className="overflow-x-auto">
                      <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                          <tr>
                            <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
                            <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase">
                              Destinatario
                            </th>
                            <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase">
                              Unidade
                            </th>
                            <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                            <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase">
                              Recebimento
                            </th>
                            <th className="px-3 py-2 text-right text-xs font-medium text-gray-500 uppercase">Acao</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200 bg-white">
                          {listaEncomendas.map((item) => {
                            const selecionada = encomendaSelecionada && Number(encomendaSelecionada.id) === Number(item.id)
                            const podeSelecionar = item.status === StatusEncomenda.RECEBIDA

                            return (
                              <tr key={item.id} className={selecionada ? 'bg-green-50' : ''}>
                                <td className="px-3 py-2 text-sm text-gray-900">#{item.id}</td>
                                <td className="px-3 py-2 text-sm text-gray-900">{item.nomeDestinatario}</td>
                                <td className="px-3 py-2 text-sm text-gray-900">
                                  {item.bloco}-{item.apartamento}
                                </td>
                                <td className="px-3 py-2 text-sm">
                                  <span
                                    className={
                                      item.status === StatusEncomenda.RECEBIDA
                                        ? 'inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800'
                                        : 'inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-gray-200 text-gray-800'
                                    }
                                  >
                                    {item.status}
                                  </span>
                                </td>
                                <td className="px-3 py-2 text-sm text-gray-700">
                                  {new Date(item.dataRecebimento).toLocaleString('pt-BR')}
                                </td>
                                <td className="px-3 py-2 text-right">
                                  <Button
                                    type="button"
                                    size="sm"
                                    variant={selecionada ? 'primary' : 'outline'}
                                    onClick={() => selecionarEncomenda(item)}
                                    disabled={!podeSelecionar}
                                  >
                                    {podeSelecionar ? 'Selecionar' : 'Retirada registrada'}
                                  </Button>
                                </td>
                              </tr>
                            )
                          })}
                        </tbody>
                      </table>
                    </div>
                  )}

                  <div className="mt-4 flex items-center justify-between">
                    <p className="text-sm text-gray-600">
                      Total de encomendas: <strong>{totalElements}</strong>
                    </p>
                    <div className="flex items-center gap-3">
                      <span className="text-sm text-gray-600">
                        Pagina {Math.min(currentPage + 1, Math.max(totalPages, 1))} de {Math.max(totalPages, 1)}
                      </span>
                      <Button
                        type="button"
                        size="sm"
                        variant="outline"
                        onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 0))}
                        disabled={currentPage === 0 || isLoadingLista}
                      >
                        Anterior
                      </Button>
                      <Button
                        type="button"
                        size="sm"
                        variant="outline"
                        onClick={() => setCurrentPage((prev) => prev + 1)}
                        disabled={isLoadingLista || totalPages === 0 || currentPage >= totalPages - 1}
                      >
                        Proxima
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {encomendaSelecionada && (
                <Card className="mb-6">
                  <CardHeader>
                    <CardTitle className="text-lg">Detalhes da encomenda selecionada</CardTitle>
                    <CardDescription>
                      Confirme os dados e registre a retirada com o nome de quem retirou.
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="bg-gray-50 rounded-lg p-4 mb-4">
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                          <p className="text-sm font-medium text-gray-500">ID</p>
                          <p className="text-lg font-semibold">#{encomendaSelecionada.id}</p>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-500">Destinatario</p>
                          <p className="text-lg">{encomendaSelecionada.nomeDestinatario}</p>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-500">Unidade</p>
                          <p className="text-lg">
                            {encomendaSelecionada.bloco}-{encomendaSelecionada.apartamento}
                          </p>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-500">Status</p>
                          <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                            {encomendaSelecionada.status}
                          </span>
                        </div>
                        <div className="md:col-span-2">
                          <p className="text-sm font-medium text-gray-500">Descricao</p>
                          <p className="text-lg">{encomendaSelecionada.descricao}</p>
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
                            required: 'Nome de quem retirou e obrigatorio',
                            minLength: {
                              value: 3,
                              message: 'Nome deve ter pelo menos 3 caracteres',
                            },
                          })}
                          type="text"
                          id="retiradoPorNome"
                          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
                          placeholder="Joao Silva"
                        />
                        {errors.retiradoPorNome && (
                          <p className="mt-1 text-sm text-red-600">{errors.retiradoPorNome.message}</p>
                        )}
                      </div>

                      <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4">
                        <div className="text-sm text-yellow-700">
                          Confirme a identificacao da pessoa antes de registrar a retirada.
                        </div>
                      </div>

                      <div className="flex justify-end space-x-4">
                        <Button
                          type="button"
                          variant="outline"
                          onClick={() => setEncomendaSelecionada(null)}
                          disabled={isLoadingRetirada}
                        >
                          Cancelar
                        </Button>
                        <Button type="submit" variant="primary" disabled={isLoadingRetirada}>
                          <Package className="w-4 h-4 mr-2" />
                          {isLoadingRetirada ? 'Registrando...' : 'Confirmar Retirada'}
                        </Button>
                      </div>
                    </form>
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
