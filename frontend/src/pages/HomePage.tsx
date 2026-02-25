import { Link } from 'react-router-dom'
import { Header } from '@/components/layout/Header'
import { Button } from '@/components/ui/Button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card'
import { abrirLinkExterno } from '@/utils'

type Documento = {
  titulo: string
  descricao: string
  url: string
  icone: string
  cor: string
}

export function HomePage() {
  const handleLinkExterno = (url: string) => {
    abrirLinkExterno(url)
  }

  const documentacoes: Documento[] = [
    {
      titulo: 'Swagger - Identidade',
      descricao: 'API de autenticacao, token e administracao de identidades.',
      url: 'http://localhost:8081/swagger-ui/index.html',
      icone: 'ID',
      cor: 'text-blue-600'
    },
    {
      titulo: 'Swagger - Usuario',
      descricao: 'API de cadastro e manutencao de usuarios do condominio.',
      url: 'http://localhost:8082/swagger-ui/index.html',
      icone: 'US',
      cor: 'text-emerald-600'
    },
    {
      titulo: 'Swagger - Encomenda',
      descricao: 'API de recebimento, consulta e baixa de encomendas.',
      url: 'http://localhost:8083/swagger-ui/index.html',
      icone: 'EN',
      cor: 'text-amber-600'
    },
    {
      titulo: 'Swagger - Notificacao',
      descricao: 'API de notificacoes para moradores e confirmacao de leitura.',
      url: 'http://localhost:8084/swagger-ui/index.html',
      icone: 'NT',
      cor: 'text-purple-600'
    }
  ]

  const javadocs: Documento[] = [
    {
      titulo: 'JavaDoc - Identidade',
      descricao: 'Classes e pacotes do servico de identidade.',
      url: 'http://localhost:8090/javadocs/servico-identidade/index.html',
      icone: 'JD',
      cor: 'text-blue-600'
    },
    {
      titulo: 'JavaDoc - Usuario',
      descricao: 'Classes e pacotes do servico de usuario.',
      url: 'http://localhost:8090/javadocs/servico-usuario/index.html',
      icone: 'JD',
      cor: 'text-emerald-600'
    },
    {
      titulo: 'JavaDoc - Encomenda',
      descricao: 'Classes e pacotes do servico de encomenda.',
      url: 'http://localhost:8090/javadocs/servico-encomenda/index.html',
      icone: 'JD',
      cor: 'text-amber-600'
    },
    {
      titulo: 'JavaDoc - Notificacao',
      descricao: 'Classes e pacotes do servico de notificacao.',
      url: 'http://localhost:8090/javadocs/servico-notificacao/index.html',
      icone: 'JD',
      cor: 'text-purple-600'
    }
  ]

  const docsTecnicos: Documento[] = [
    {
      titulo: 'Guia Docker',
      descricao: 'Manual tecnico de execucao e operacao via Docker.',
      url: 'http://localhost:8090/DOCKER.md',
      icone: 'DC',
      cor: 'text-cyan-600'
    }
  ]

  return (
    <div className="min-h-screen bg-gray-50">
      <Header mostrarLinksExternos={true} />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="text-center mb-16">
          <h1 className="text-4xl font-bold text-gray-900 sm:text-5xl md:text-6xl">
            Bem-vindo ao
            <span className="text-primary-600"> Sistema Condominio</span>
          </h1>
          <p className="mt-3 max-w-md mx-auto text-base text-gray-500 sm:text-lg md:mt-5 md:text-xl md:max-w-3xl">
            Plataforma moderna para gestao condominial com recebimento de encomendas,
            notificacoes e controle de acesso.
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

        <div className="mb-16">
          <div className="text-center mb-8">
            <h2 className="text-3xl font-bold text-gray-900">Documentacao de APIs</h2>
            <p className="mt-2 text-gray-600">
              Acesso direto ao Swagger de cada microservico.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {documentacoes.map((doc) => (
              <Card
                key={doc.titulo}
                className="hover:shadow-lg transition-shadow cursor-pointer group"
                onClick={() => handleLinkExterno(doc.url)}
              >
                <CardHeader className="text-center">
                  <div className={`text-2xl font-bold mb-4 ${doc.cor} group-hover:scale-110 transition-transform`}>
                    {doc.icone}
                  </div>
                  <CardTitle className="text-xl">{doc.titulo}</CardTitle>
                  <CardDescription>{doc.descricao}</CardDescription>
                </CardHeader>
                <CardContent>
                  <Button
                    variant="outline"
                    className="w-full group-hover:bg-primary-50 group-hover:border-primary-300 group-hover:text-primary-700 transition-colors"
                  >
                    Abrir Swagger
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>

        <div className="mb-16">
          <div className="text-center mb-8">
            <h2 className="text-3xl font-bold text-gray-900">JavaDoc por servico</h2>
            <p className="mt-2 text-gray-600">
              Acesso ao JavaDoc de cada microservico.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {javadocs.map((doc) => (
              <Card
                key={doc.titulo}
                className="hover:shadow-lg transition-shadow cursor-pointer group"
                onClick={() => handleLinkExterno(doc.url)}
              >
                <CardHeader className="text-center">
                  <div className={`text-2xl font-bold mb-4 ${doc.cor} group-hover:scale-110 transition-transform`}>
                    {doc.icone}
                  </div>
                  <CardTitle className="text-xl">{doc.titulo}</CardTitle>
                  <CardDescription>{doc.descricao}</CardDescription>
                </CardHeader>
                <CardContent>
                  <Button
                    variant="outline"
                    className="w-full group-hover:bg-primary-50 group-hover:border-primary-300 group-hover:text-primary-700 transition-colors"
                  >
                    Abrir JavaDoc
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>

        <div className="mb-16">
          <div className="text-center mb-8">
            <h2 className="text-3xl font-bold text-gray-900">Documentacao Tecnica</h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {docsTecnicos.map((doc) => (
              <Card
                key={doc.titulo}
                className="hover:shadow-lg transition-shadow cursor-pointer group"
                onClick={() => handleLinkExterno(doc.url)}
              >
                <CardHeader className="text-center">
                  <div className={`text-2xl font-bold mb-4 ${doc.cor} group-hover:scale-110 transition-transform`}>
                    {doc.icone}
                  </div>
                  <CardTitle className="text-xl">{doc.titulo}</CardTitle>
                  <CardDescription>{doc.descricao}</CardDescription>
                </CardHeader>
                <CardContent>
                  <Button
                    variant="outline"
                    className="w-full group-hover:bg-primary-50 group-hover:border-primary-300 group-hover:text-primary-700 transition-colors"
                  >
                    Abrir Documento
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-8">
          <div className="text-center mb-8">
            <h2 className="text-3xl font-bold text-gray-900">Funcionalidades</h2>
            <p className="mt-2 text-gray-600">
              Conheca as principais funcionalidades do sistema.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="text-center">
              <div className="text-3xl mb-3">UM</div>
              <h3 className="font-semibold text-gray-900">Gestao de Usuarios</h3>
              <p className="text-sm text-gray-600 mt-1">
                Cadastro de moradores e funcionarios.
              </p>
            </div>
            <div className="text-center">
              <div className="text-3xl mb-3">EN</div>
              <h3 className="font-semibold text-gray-900">Recebimento Encomendas</h3>
              <p className="text-sm text-gray-600 mt-1">
                Controle completo de recebimento e retirada.
              </p>
            </div>
            <div className="text-center">
              <div className="text-3xl mb-3">NF</div>
              <h3 className="font-semibold text-gray-900">Notificacoes</h3>
              <p className="text-sm text-gray-600 mt-1">
                Sistema de avisos e comunicados.
              </p>
            </div>
            <div className="text-center">
              <div className="text-3xl mb-3">SG</div>
              <h3 className="font-semibold text-gray-900">Seguranca</h3>
              <p className="text-sm text-gray-600 mt-1">
                Autenticacao e controle de acesso.
              </p>
            </div>
          </div>
        </div>

        <div className="mt-12 text-center">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">
            Acesso rapido as ferramentas
          </h3>
          <div className="flex flex-wrap justify-center gap-3">
            <Button
              variant="ghost"
              onClick={() => handleLinkExterno('http://localhost:5601')}
              className="text-sm"
            >
              Kibana (Logs)
            </Button>
            <Button
              variant="ghost"
              onClick={() => handleLinkExterno('http://localhost:8086')}
              className="text-sm"
            >
              Kafka UI
            </Button>
            <Button
              variant="ghost"
              onClick={() => handleLinkExterno('http://localhost:8087')}
              className="text-sm"
            >
              Adminer (DB)
            </Button>
          </div>
        </div>
      </main>

      <footer className="bg-white border-t border-gray-200 mt-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="text-center text-gray-500 text-sm">
            <p>2026 Sistema Condominio. Desenvolvido com React, Spring Boot e Docker.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}
