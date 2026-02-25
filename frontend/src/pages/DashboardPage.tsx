import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Header } from '@/components/layout/Header'
import { Button } from '@/components/ui/Button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card'
import { encomendaService } from '@/services/encomendaService'
import { usuarioService } from '@/services/usuarioService'
import { Usuario, TipoUsuario } from '@/types'
import { Package, Bell, User, Home } from 'lucide-react'

interface DashboardPageProps {
  onLogout?: () => void
}

export function DashboardPage({ onLogout }: DashboardPageProps) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string>('')
  const [movimentacoesHoje, setMovimentacoesHoje] = useState<number>(0)
  const [movimentacoesOntem, setMovimentacoesOntem] = useState<number>(0)
  const [carregandoMovimentacoes, setCarregandoMovimentacoes] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    carregarDadosUsuario()
  }, [])

  const carregarDadosUsuario = async () => {
    try {
      setLoading(true)
      const dadosUsuario = await usuarioService.getMeuPerfil()
      setUsuario(dadosUsuario)

      // Atualizar dados no localStorage
      localStorage.setItem('user_data', JSON.stringify(dadosUsuario))

      if (dadosUsuario.tipo === TipoUsuario.FUNCIONARIO) {
        await carregarMovimentacoesFuncionario()
      }
    } catch (err: any) {
      setError(err.message || 'Erro ao carregar dados do usuário')
      if (err.message.includes('Não autorizado')) {
        // Token expirado, redirecionar para login
        handleLogout()
      }
    } finally {
      setLoading(false)
    }
  }

  const formatarDataLocal = (data: Date) => {
    const ano = data.getFullYear()
    const mes = String(data.getMonth() + 1).padStart(2, '0')
    const dia = String(data.getDate()).padStart(2, '0')
    return `${ano}-${mes}-${dia}`
  }

  const carregarMovimentacoesFuncionario = async () => {
    setCarregandoMovimentacoes(true)
    try {
      const hoje = new Date()
      const ontem = new Date()
      ontem.setDate(hoje.getDate() - 1)

      const [respostaHoje, respostaOntem] = await Promise.all([
        encomendaService.listarEncomendas({
          data: formatarDataLocal(hoje),
          page: 0,
          size: 1,
        }),
        encomendaService.listarEncomendas({
          data: formatarDataLocal(ontem),
          page: 0,
          size: 1,
        }),
      ])

      setMovimentacoesHoje(respostaHoje.totalElements)
      setMovimentacoesOntem(respostaOntem.totalElements)
    } catch {
      setMovimentacoesHoje(0)
      setMovimentacoesOntem(0)
    } finally {
      setCarregandoMovimentacoes(false)
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('auth_token')
    localStorage.removeItem('user_data')
    
    if (onLogout) {
      onLogout()
    }
    
    navigate('/')
  }

  const handleMeusDados = () => {
    navigate('/meus-dados')
  }

  const handleReceberEncomenda = () => {
    navigate('/receber-encomenda')
  }

  const handleRetirarEncomenda = () => {
    navigate('/retirar-encomenda')
  }

  const handleConfirmarNotificacoes = () => {
    navigate('/confirmar-notificacoes')
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header 
          usuarioLogado={true}
          nomeUsuario={usuario?.nomeCompleto || ''}
          onLogout={handleLogout}
          onMeusDados={handleMeusDados}
          mostrarLinksExternos={true}
        />
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
            <p className="mt-4 text-gray-600">Carregando...</p>
          </div>
        </main>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header 
          usuarioLogado={true}
          nomeUsuario={usuario?.nomeCompleto || ''}
          onLogout={handleLogout}
          onMeusDados={handleMeusDados}
          mostrarLinksExternos={true}
        />
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="text-center">
            <div className="bg-red-50 border border-red-200 rounded-md p-4 max-w-md mx-auto">
              <h3 className="text-sm font-medium text-red-800">Erro</h3>
              <p className="mt-2 text-sm text-red-700">{error}</p>
              <Button
                variant="primary"
                onClick={carregarDadosUsuario}
                className="mt-4"
              >
                Tentar Novamente
              </Button>
            </div>
          </div>
        </main>
      </div>
    )
  }

  const isFuncionario = usuario?.tipo === TipoUsuario.FUNCIONARIO
  const isMorador = usuario?.tipo === TipoUsuario.MORADOR

  return (
    <div className="min-h-screen bg-gray-50">
      <Header 
        usuarioLogado={true}
        nomeUsuario={usuario?.nomeCompleto || ''}
        onLogout={handleLogout}
        onMeusDados={handleMeusDados}
        mostrarLinksExternos={true}
      />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Welcome Section */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">
            Bem-vindo(a), {usuario?.nomeCompleto}!
          </h1>
          <p className="mt-2 text-gray-600">
            {isFuncionario 
              ? 'Painel do Funcionário - Gerencie as operações do condomínio'
              : 'Painel do Morador - Acompanhe suas notificações e encomendas'
            }
          </p>
        </div>

        {/* Funcionário Dashboard */}
        {isFuncionario && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {/* Receber Encomenda */}
            <Card className="hover:shadow-lg transition-shadow cursor-pointer group">
              <CardHeader>
                <div className="flex items-center space-x-3">
                  <div className="p-3 bg-blue-100 rounded-lg group-hover:bg-blue-200 transition-colors">
                    <Package className="h-6 w-6 text-blue-600" />
                  </div>
                  <div>
                    <CardTitle className="text-lg">Receber Encomenda</CardTitle>
                    <CardDescription>
                      Registre novas encomendas recebidas na portaria
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <Button 
                  variant="primary" 
                  onClick={handleReceberEncomenda}
                  className="w-full"
                >
                  Registrar Recebimento
                </Button>
              </CardContent>
            </Card>

            {/* Retirar Encomenda */}
            <Card className="hover:shadow-lg transition-shadow cursor-pointer group">
              <CardHeader>
                <div className="flex items-center space-x-3">
                  <div className="p-3 bg-green-100 rounded-lg group-hover:bg-green-200 transition-colors">
                    <Package className="h-6 w-6 text-green-600" />
                  </div>
                  <div>
                    <CardTitle className="text-lg">Retirar Encomenda</CardTitle>
                    <CardDescription>
                      Registre a retirada de encomendas pelos moradores
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <Button 
                  variant="primary" 
                  onClick={handleRetirarEncomenda}
                  className="w-full"
                >
                  Registrar Retirada
                </Button>
              </CardContent>
            </Card>

            {/* Últimas Movimentações */}
            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-center space-x-3">
                  <div className="p-3 bg-purple-100 rounded-lg">
                    <Home className="h-6 w-6 text-purple-600" />
                  </div>
                  <div>
                    <CardTitle className="text-lg">Movimentações</CardTitle>
                    <CardDescription>
                      Últimas atividades do sistema
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <div className="text-sm">
                    <span className="text-gray-500">Hoje:</span>
                    <span className="ml-2 text-gray-900">{carregandoMovimentacoes ? 'Carregando...' : `${movimentacoesHoje} registros`}</span>
                  </div>
                  <div className="text-sm">
                    <span className="text-gray-500">Ontem:</span>
                    <span className="ml-2 text-gray-900">{carregandoMovimentacoes ? 'Carregando...' : `${movimentacoesOntem} registros`}</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        )}

        {/* Morador Dashboard */}
        {isMorador && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {/* Confirmar Notificações */}
            <Card className="hover:shadow-lg transition-shadow cursor-pointer group">
              <CardHeader>
                <div className="flex items-center space-x-3">
                  <div className="p-3 bg-orange-100 rounded-lg group-hover:bg-orange-200 transition-colors">
                    <Bell className="h-6 w-6 text-orange-600" />
                  </div>
                  <div>
                    <CardTitle className="text-lg">Notificações</CardTitle>
                    <CardDescription>
                      Confirme o recebimento de suas notificações
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <Button 
                  variant="primary" 
                  onClick={handleConfirmarNotificacoes}
                  className="w-full"
                >
                  Gerenciar Notificações
                </Button>
              </CardContent>
            </Card>

            {/* Dados Residenciais */}
            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-center space-x-3">
                  <div className="p-3 bg-green-100 rounded-lg">
                    <Home className="h-6 w-6 text-green-600" />
                  </div>
                  <div>
                    <CardTitle className="text-lg">Meus Dados</CardTitle>
                    <CardDescription>
                      Informações do seu perfil
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <div className="text-sm">
                    <span className="text-gray-500">Apartamento:</span>
                    <span className="ml-2 text-gray-900">{usuario?.apartamento} {usuario?.bloco}</span>
                  </div>
                  <div className="text-sm">
                    <span className="text-gray-500">Email:</span>
                    <span className="ml-2 text-gray-900">{usuario?.email}</span>
                  </div>
                </div>
                <Button 
                  variant="outline" 
                  onClick={handleMeusDados}
                  className="w-full mt-4"
                >
                  Editar Perfil
                </Button>
              </CardContent>
            </Card>
          </div>
        )}

        {/* Quick Actions */}
        <div className="mt-8">
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Ações Rápidas</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-3">
                <Button variant="ghost" onClick={handleMeusDados}>
                  <User className="w-4 h-4 mr-2" />
                  Meus Dados
                </Button>
                <Button variant="ghost" onClick={() => window.open('http://localhost:5601', '_blank')}>
                  📊 Kibana
                </Button>
                <Button variant="ghost" onClick={() => window.open('http://localhost:8086', '_blank')}>
                  🔧 Kafka UI
                </Button>
                <Button variant="ghost" onClick={() => window.open('http://localhost:8087', '_blank')}>
                  🗄️ Adminer
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  )
}
