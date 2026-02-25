import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Header } from '@/components/layout/Header'
import { Button } from '@/components/ui/Button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card'
import { notificacaoService } from '@/services/notificacaoService'
import { Notificacao } from '@/types'
import { ArrowLeft, Bell, CheckCircle, AlertCircle, ChevronLeft, ChevronRight, Loader2 } from 'lucide-react'

export function ConfirmarNotificacoesPage() {
  const [notificacoes, setNotificacoes] = useState<Notificacao[]>([])
  const [selectedNotificacoes, setSelectedNotificacoes] = useState<string[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isConfirming, setIsConfirming] = useState(false)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [pageSize] = useState(10)
  const [error, setError] = useState<string>('')
  const [successMessage, setSuccessMessage] = useState<string>('')
  const navigate = useNavigate()

  useEffect(() => {
    carregarNotificacoes()
  }, [currentPage])

  const carregarNotificacoes = async () => {
    try {
      setIsLoading(true)
      setError('')
      
      const response = await notificacaoService.listarNotificacoesPendentes(
        currentPage,
        pageSize,
        false // Apenas não confirmadas
      )
      
      setNotificacoes(response.notificacoes)
      setTotalPages(Math.ceil(response.notificacoes.length / pageSize))
    } catch (err: any) {
      setError(err.message || 'Erro ao carregar notificações')
    } finally {
      setIsLoading(false)
    }
  }

  const toggleNotificacaoSelection = (notificacaoId: string) => {
    setSelectedNotificacoes(prev => {
      if (prev.includes(notificacaoId)) {
        return prev.filter(id => id !== notificacaoId)
      } else {
        return [...prev, notificacaoId]
      }
    })
  }

  const toggleSelectAll = () => {
    if (selectedNotificacoes.length === notificacoes.length) {
      setSelectedNotificacoes([])
    } else {
      setSelectedNotificacoes(notificacoes.map(n => n.id.toString()))
    }
  }

  const confirmarSelecionadas = async () => {
    if (selectedNotificacoes.length === 0) {
      setError('Selecione pelo menos uma notificação para confirmar')
      return
    }

    setIsConfirming(true)
    setError('')
    setSuccessMessage('')

    try {
      const result = await notificacaoService.confirmarNotificacoesLote(selectedNotificacoes)
      
      if (result.falhas.length > 0) {
        setError(`Algumas notificações não puderam ser confirmadas: ${result.falhas.map(f => f.id).join(', ')}`)
      } else {
        setSuccessMessage(`${result.sucesso.length} notificação(ões) confirmada(s) com sucesso!`)
        setSelectedNotificacoes([])
      }
      
      // Recarregar lista
      await carregarNotificacoes()
      
      // Limpar mensagem de sucesso após 3 segundos
      setTimeout(() => setSuccessMessage(''), 3000)
    } catch (err: any) {
      setError(err.message || 'Erro ao confirmar notificações')
    } finally {
      setIsConfirming(false)
    }
  }

  const confirmarIndividual = async (notificacaoId: string) => {
    try {
      await notificacaoService.confirmarNotificacao(notificacaoId)
      
      // Remover da lista
      setNotificacoes(prev => prev.filter(n => n.id.toString() !== notificacaoId))
      setSelectedNotificacoes(prev => prev.filter(id => id !== notificacaoId))
      
      setSuccessMessage('Notificação confirmada com sucesso!')
      setTimeout(() => setSuccessMessage(''), 3000)
    } catch (err: any) {
      setError(err.message || 'Erro ao confirmar notificação')
    }
  }

  const handleVoltar = () => {
    navigate('/dashboard')
  }

  const formatarData = (dataString: string) => {
    return new Date(dataString).toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getCanalIcon = (canal: string) => {
    switch (canal) {
      case 'EMAIL': return '📧'
      case 'SMS': return '📱'
      case 'WHATSAPP': return '💬'
      default: return '📢'
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
        <div className="max-w-4xl mx-auto">
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
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <div className="p-3 bg-orange-100 rounded-lg">
                  <Bell className="h-6 w-6 text-orange-600" />
                </div>
                <div>
                  <h1 className="text-2xl font-bold text-gray-900">
                    Confirmar Notificações
                  </h1>
                  <p className="text-gray-600">
                    Confirme o recebimento de suas notificações pendentes
                  </p>
                </div>
              </div>
              
              {notificacoes.length > 0 && (
                <div className="flex items-center space-x-2 text-sm text-gray-600">
                  <span>{selectedNotificacoes.length} selecionadas</span>
                  <span>•</span>
                  <span>{notificacoes.length} totais</span>
                </div>
              )}
            </div>
          </div>

          {/* Success Message */}
          {successMessage && (
            <div className="bg-green-50 border border-green-200 rounded-md p-4 mb-6">
              <div className="flex">
                <div className="flex-shrink-0">
                  <CheckCircle className="h-5 w-5 text-green-400" />
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-green-800">
                    Sucesso!
                  </h3>
                  <div className="mt-2 text-sm text-green-700">
                    {successMessage}
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

          {/* Loading */}
          {isLoading ? (
            <Card>
              <CardContent className="flex items-center justify-center py-12">
                <Loader2 className="h-8 w-8 animate-spin text-primary-600 mr-3" />
                <span className="text-gray-600">Carregando notificações...</span>
              </CardContent>
            </Card>
          ) : notificacoes.length === 0 ? (
            <Card>
              <CardContent className="text-center py-12">
                <div className="p-3 bg-gray-100 rounded-full w-12 h-12 mx-auto mb-4 flex items-center justify-center">
                  <Bell className="h-6 w-6 text-gray-400" />
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  Nenhuma notificação pendente
                </h3>
                <p className="text-gray-600 mb-4">
                  Você não possui notificações aguardando confirmação no momento.
                </p>
                <Button onClick={handleVoltar}>
                  Voltar ao Dashboard
                </Button>
              </CardContent>
            </Card>
          ) : (
            <>
              {/* Ações em Lote */}
              {selectedNotificacoes.length > 0 && (
                <Card className="mb-6 border-primary-200 bg-primary-50">
                  <CardContent className="py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-4">
                        <span className="text-sm font-medium text-primary-800">
                          {selectedNotificacoes.length} notificação(ões) selecionada(s)
                        </span>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setSelectedNotificacoes([])}
                        >
                          Limpar seleção
                        </Button>
                      </div>
                      <Button
                        variant="primary"
                        onClick={confirmarSelecionadas}
                        disabled={isConfirming}
                      >
                        {isConfirming ? (
                          <>
                            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                            Confirmando...
                          </>
                        ) : (
                          <>
                            <CheckCircle className="w-4 h-4 mr-2" />
                            Confirmar Selecionadas
                          </>
                        )}
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              )}

              {/* Lista de Notificações */}
              <Card>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div>
                      <CardTitle className="text-lg">Notificações Pendentes</CardTitle>
                      <CardDescription>
                        Selecione as notificações que deseja confirmar
                      </CardDescription>
                    </div>
                    <label className="flex items-center space-x-2 text-sm">
                      <input
                        type="checkbox"
                        checked={selectedNotificacoes.length === notificacoes.length && notificacoes.length > 0}
                        onChange={toggleSelectAll}
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                      <span>Selecionar todas</span>
                    </label>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {notificacoes.map((notificacao) => (
                      <div
                        key={notificacao.id}
                        className={`border rounded-lg p-4 transition-colors ${
                          selectedNotificacoes.includes(notificacao.id.toString())
                            ? 'border-primary-500 bg-primary-50'
                            : 'border-gray-200 hover:bg-gray-50'
                        }`}
                      >
                        <div className="flex items-start space-x-3">
                          <input
                            type="checkbox"
                            checked={selectedNotificacoes.includes(notificacao.id.toString())}
                            onChange={() => toggleNotificacaoSelection(notificacao.id.toString())}
                            className="mt-1 rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                          />
                          <div className="flex-1">
                            <div className="flex items-center space-x-2 mb-2">
                              <span className="text-lg">{getCanalIcon(notificacao.canal)}</span>
                              <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-yellow-100 text-yellow-800">
                                {notificacao.status}
                              </span>
                              <span className="text-xs text-gray-500">
                                {formatarData(notificacao.criadaEm)}
                              </span>
                            </div>
                            <p className="text-gray-900 font-medium mb-1">
                              {notificacao.mensagem}
                            </p>
                            <div className="flex items-center justify-between mt-3">
                              <div className="text-sm text-gray-600">
                                <span>Canal: {notificacao.canal}</span>
                                {notificacao.destino && (
                                  <span className="ml-4">Para: {notificacao.destino}</span>
                                )}
                              </div>
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => confirmarIndividual(notificacao.id.toString())}
                              >
                                <CheckCircle className="w-3 h-3 mr-1" />
                                Confirmar
                              </Button>
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Paginação */}
                  {totalPages > 1 && (
                    <div className="flex items-center justify-between mt-6 pt-6 border-t">
                      <div className="text-sm text-gray-600">
                        Página {currentPage + 1} de {totalPages}
                      </div>
                      <div className="flex space-x-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                          disabled={currentPage === 0}
                        >
                          <ChevronLeft className="w-4 h-4 mr-1" />
                          Anterior
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                          disabled={currentPage === totalPages - 1}
                        >
                          Próxima
                          <ChevronRight className="w-4 h-4 ml-1" />
                        </Button>
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>
            </>
          )}
        </div>
      </main>
    </div>
  )
}
