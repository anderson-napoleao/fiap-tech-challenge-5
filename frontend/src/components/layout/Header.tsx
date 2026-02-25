import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Menu, X, ExternalLink, LogOut, User } from 'lucide-react'
import { Button } from '@/components/ui/Button'
import { abrirLinkExterno } from '@/utils'

interface HeaderProps {
  usuarioLogado?: boolean
  nomeUsuario?: string
  onLogout?: () => void
  onMeusDados?: () => void
  mostrarLinksExternos?: boolean
}

export function Header({ 
  usuarioLogado = false, 
  nomeUsuario = '', 
  onLogout,
  onMeusDados,
  mostrarLinksExternos = true 
}: HeaderProps) {
  const [mobileMenuOpen, setMobileMenuOpen] = React.useState(false)
  const navigate = useNavigate()

  const toggleMobileMenu = () => {
    setMobileMenuOpen(!mobileMenuOpen)
  }

  const handleLinkExterno = (url: string) => {
    abrirLinkExterno(url)
  }

  const handleLoginClick = () => {
    navigate('/login')
  }

  const handleSignUpClick = () => {
    navigate('/cadastro')
  }

  const handleLogoutClick = () => {
    if (onLogout) {
      onLogout()
    } else {
      navigate('/')
    }
  }

  const handleMeusDadosClick = () => {
    if (onMeusDados) {
      onMeusDados()
    } else {
      navigate('/meus-dados')
    }
  }

  return (
    <header className="bg-white border-b border-gray-200 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <div className="flex items-center">
            <Link to="/" className="flex-shrink-0">
              <h1 className="text-xl font-bold text-primary-600 hover:text-primary-700 transition-colors">
                🏢 Sistema Condomínio
              </h1>
            </Link>
          </div>

          {/* Desktop Menu */}
          <nav className="hidden md:flex space-x-8">
            {usuarioLogado ? (
              <>
                <button
                  onClick={handleMeusDadosClick}
                  className="flex items-center text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium transition-colors"
                >
                  <User className="w-4 h-4 mr-2" />
                  Meus Dados
                </button>
                <button
                  onClick={handleLogoutClick}
                  className="flex items-center text-gray-700 hover:text-red-600 px-3 py-2 rounded-md text-sm font-medium transition-colors"
                >
                  <LogOut className="w-4 h-4 mr-2" />
                  Sair
                </button>
              </>
            ) : (
              <>
                <Button variant="ghost" onClick={handleLoginClick}>
                  Entrar
                </Button>
                <Button variant="primary" onClick={handleSignUpClick}>
                  Cadastrar
                </Button>
              </>
            )}
          </nav>

          {/* Links Externos Desktop */}
          {mostrarLinksExternos && (
            <div className="hidden md:flex items-center space-x-4">
              <span className="text-sm text-gray-500">Ferramentas:</span>
              <button
                onClick={() => handleLinkExterno('http://localhost:5601')}
                className="flex items-center text-gray-600 hover:text-primary-600 text-sm transition-colors"
                title="Kibana - Logs"
              >
                📊 Kibana
                <ExternalLink className="w-3 h-3 ml-1" />
              </button>
              <button
                onClick={() => handleLinkExterno('http://localhost:8086')}
                className="flex items-center text-gray-600 hover:text-primary-600 text-sm transition-colors"
                title="Kafka UI - Mensageria"
              >
                🔧 Kafka UI
                <ExternalLink className="w-3 h-3 ml-1" />
              </button>
              <button
                onClick={() => handleLinkExterno('http://localhost:8087')}
                className="flex items-center text-gray-600 hover:text-primary-600 text-sm transition-colors"
                title="Adminer - Banco de Dados"
              >
                🗄️ Adminer
                <ExternalLink className="w-3 h-3 ml-1" />
              </button>
            </div>
          )}

          {/* Mobile menu button */}
          <div className="md:hidden">
            <button
              onClick={toggleMobileMenu}
              className="inline-flex items-center justify-center p-2 rounded-md text-gray-700 hover:text-primary-600 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-primary-500"
            >
              {mobileMenuOpen ? (
                <X className="block h-6 w-6" />
              ) : (
                <Menu className="block h-6 w-6" />
              )}
            </button>
          </div>
        </div>

        {/* Mobile Menu */}
        {mobileMenuOpen && (
          <div className="md:hidden">
            <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3 border-t border-gray-200">
              {usuarioLogado ? (
                <>
                  <div className="px-3 py-2 text-sm text-gray-700">
                    👤 {nomeUsuario}
                  </div>
                  <button
                    onClick={handleMeusDadosClick}
                    className="flex items-center w-full text-left px-3 py-2 text-gray-700 hover:text-primary-600 rounded-md text-base font-medium"
                  >
                    <User className="w-4 h-4 mr-2" />
                    Meus Dados
                  </button>
                  <button
                    onClick={handleLogoutClick}
                    className="flex items-center w-full text-left px-3 py-2 text-gray-700 hover:text-red-600 rounded-md text-base font-medium"
                  >
                    <LogOut className="w-4 h-4 mr-2" />
                    Sair
                  </button>
                </>
              ) : (
                <>
                  <button
                    onClick={handleLoginClick}
                    className="block w-full text-left px-3 py-2 text-gray-700 hover:text-primary-600 rounded-md text-base font-medium"
                  >
                    Entrar
                  </button>
                  <button
                    onClick={handleSignUpClick}
                    className="block w-full text-left px-3 py-2 text-gray-700 hover:text-primary-600 rounded-md text-base font-medium"
                  >
                    Cadastrar
                  </button>
                </>
              )}
              
              {mostrarLinksExternos && (
                <>
                  <div className="border-t border-gray-200 my-2"></div>
                  <div className="px-3 py-2 text-sm font-medium text-gray-500">
                    Ferramentas (abrem em nova aba):
                  </div>
                  <button
                    onClick={() => handleLinkExterno('http://localhost:5601')}
                    className="flex items-center w-full text-left px-3 py-2 text-gray-600 hover:text-primary-600 rounded-md text-sm"
                  >
                    📊 Kibana - Logs
                    <ExternalLink className="w-3 h-3 ml-1" />
                  </button>
                  <button
                    onClick={() => handleLinkExterno('http://localhost:8086')}
                    className="flex items-center w-full text-left px-3 py-2 text-gray-600 hover:text-primary-600 rounded-md text-sm"
                  >
                    🔧 Kafka UI - Mensageria
                    <ExternalLink className="w-3 h-3 ml-1" />
                  </button>
                  <button
                    onClick={() => handleLinkExterno('http://localhost:8087')}
                    className="flex items-center w-full text-left px-3 py-2 text-gray-600 hover:text-primary-600 rounded-md text-sm"
                  >
                    🗄️ Adminer - Banco de Dados
                    <ExternalLink className="w-3 h-3 ml-1" />
                  </button>
                </>
              )}
            </div>
          </div>
        )}
      </div>
    </header>
  )
}
