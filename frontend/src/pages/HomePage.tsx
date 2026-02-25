import { Link } from 'react-router-dom'
import { Header } from '@/components/layout/Header'
import { Button } from '@/components/ui/Button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card'
import { abrirLinkExterno } from '@/utils'

export function HomePage() {
  const handleLinkExterno = (url: string) => {
    abrirLinkExterno(url)
  }

  const documentacoes = [
    {
      titulo: 'Swagger UI',
      descricao: 'Documentação interativa das APIs REST',
      url: 'http://localhost:8081/swagger-ui.html',
      icone: '📚',
      cor: 'text-blue-600'
    },
    {
      titulo: 'JavaDocs',
      descricao: 'Documentação técnica do código fonte',
      url: 'http://localhost:8081/javadoc',
      icone: '📖',
      cor: 'text-green-600'
    },
    {
      titulo: 'Docker Docs',
      descricao: 'Documentação da infraestrutura Docker',
      url: 'https://github.com/usuario/sistema-condominio/blob/main/DOCKER.md',
      icone: '🐳',
      cor: 'text-cyan-600'
    }
  ]

  return (
    <div className="min-h-screen bg-gray-50">
      <Header 
        mostrarLinksExternos={true}
      />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Hero Section */}
        <div className="text-center mb-16">
          <h1 className="text-4xl font-bold text-gray-900 sm:text-5xl md:text-6xl">
            Bem-vindo ao
            <span className="text-primary-600"> Sistema Condomínio</span>
          </h1>
          <p className="mt-3 max-w-md mx-auto text-base text-gray-500 sm:text-lg md:mt-5 md:text-xl md:max-w-3xl">
            Plataforma moderna para gestão condominial com recebimento de encomendas, 
            notificações e controle de acesso.
          </p>
          <div className="mt-8 flex justify-center space-x-4">
            <Link to="/login">
              <Button variant="primary" size="lg">
                Entrar
              </Button>
            </Link>
            <Link to="/cadastro">
              <Button variant="outline" size="lg">
                Cadastrar
              </Button>
            </Link>
          </div>
        </div>

        {/* Documentação Section */}
        <div className="mb-16">
          <div className="text-center mb-8">
            <h2 className="text-3xl font-bold text-gray-900">
              📚 Documentação
            </h2>
            <p className="mt-2 text-gray-600">
              Explore a documentação técnica e APIs do sistema
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {documentacoes.map((doc, index) => (
              <Card 
                key={index} 
                className="hover:shadow-lg transition-shadow cursor-pointer group"
                onClick={() => handleLinkExterno(doc.url)}
              >
                <CardHeader className="text-center">
                  <div className={`text-4xl mb-4 ${doc.cor} group-hover:scale-110 transition-transform`}>
                    {doc.icone}
                  </div>
                  <CardTitle className="text-xl">{doc.titulo}</CardTitle>
                  <CardDescription>
                    {doc.descricao}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <Button 
                    variant="outline" 
                    className="w-full group-hover:bg-primary-50 group-hover:border-primary-300 group-hover:text-primary-700 transition-colors"
                  >
                    Acessar Documentação
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>

        {/* Features Section */}
        <div className="bg-white rounded-lg shadow-sm p-8">
          <div className="text-center mb-8">
            <h2 className="text-3xl font-bold text-gray-900">
              🚀 Funcionalidades
            </h2>
            <p className="mt-2 text-gray-600">
              Conheça as principais funcionalidades do sistema
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="text-center">
              <div className="text-3xl mb-3">👤</div>
              <h3 className="font-semibold text-gray-900">Gestão de Usuários</h3>
              <p className="text-sm text-gray-600 mt-1">
                Cadastro de moradores e funcionários
              </p>
            </div>
            <div className="text-center">
              <div className="text-3xl mb-3">📦</div>
              <h3 className="font-semibold text-gray-900">Recebimento Encomendas</h3>
              <p className="text-sm text-gray-600 mt-1">
                Controle completo de recebimento e retirada
              </p>
            </div>
            <div className="text-center">
              <div className="text-3xl mb-3">🔔</div>
              <h3 className="font-semibold text-gray-900">Notificações</h3>
              <p className="text-sm text-gray-600 mt-1">
                Sistema de avisos e comunicados
              </p>
            </div>
            <div className="text-center">
              <div className="text-3xl mb-3">🔐</div>
              <h3 className="font-semibold text-gray-900">Segurança</h3>
              <p className="text-sm text-gray-600 mt-1">
                Autenticação e controle de acesso
              </p>
            </div>
          </div>
        </div>

        {/* Quick Access */}
        <div className="mt-12 text-center">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">
            Acesso Rápido às Ferramentas
          </h3>
          <div className="flex flex-wrap justify-center gap-3">
            <Button
              variant="ghost"
              onClick={() => handleLinkExterno('http://localhost:5601')}
              className="text-sm"
            >
              📊 Kibana (Logs)
            </Button>
            <Button
              variant="ghost"
              onClick={() => handleLinkExterno('http://localhost:8086')}
              className="text-sm"
            >
              🔧 Kafka UI
            </Button>
            <Button
              variant="ghost"
              onClick={() => handleLinkExterno('http://localhost:8087')}
              className="text-sm"
            >
              🗄️ Adminer (DB)
            </Button>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 mt-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="text-center text-gray-500 text-sm">
            <p>© 2024 Sistema Condomínio. Desenvolvido com ❤️ usando React, Spring Boot e Docker.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}
