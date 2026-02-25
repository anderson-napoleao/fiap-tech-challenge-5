import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { HomePage } from '@/pages/HomePage'
import { LoginPage } from '@/pages/LoginPage'
import { SignUpPage } from '@/pages/SignUpPage'
import { DashboardPage } from '@/pages/DashboardPage'
import { MeusDadosPage } from '@/pages/MeusDadosPage'
import { ReceberEncomendaPage } from '@/pages/ReceberEncomendaPage'
import { RetirarEncomendaPage } from '@/pages/RetirarEncomendaPage'
import { ConfirmarNotificacoesPage } from '@/pages/ConfirmarNotificacoesPage'
import { authService } from '@/services/authService'

// Componente para rotas protegidas
function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('auth_token')
  
  if (!token) {
    return <Navigate to="/login" replace />
  }

  // Verificar se token está expirado
  if (authService.isTokenExpired(token)) {
    localStorage.removeItem('auth_token')
    localStorage.removeItem('user_data')
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}

// Componente para redirecionar usuário logado
function PublicRoute({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('auth_token')
  
  if (token && !authService.isTokenExpired(token)) {
    return <Navigate to="/dashboard" replace />
  }

  return <>{children}</>
}

function App() {
  const handleLogin = (_userData: any) => {
    // Dados já são persistidos no fluxo de login.
  }

  const handleLogout = () => {
    localStorage.removeItem('auth_token')
    localStorage.removeItem('user_data')
  }

  return (
    <Router>
      <div className="App">
        <Routes>
          {/* Rotas Públicas */}
          <Route 
            path="/" 
            element={
              <PublicRoute>
                <HomePage />
              </PublicRoute>
            } 
          />
          
          <Route 
            path="/home" 
            element={
              <PublicRoute>
                <HomePage />
              </PublicRoute>
            } 
          />

          <Route 
            path="/login" 
            element={
              <PublicRoute>
                <LoginPage onLoginSuccess={handleLogin} />
              </PublicRoute>
            } 
          />

          <Route 
            path="/cadastro" 
            element={
              <PublicRoute>
                <SignUpPage onSignUpSuccess={handleLogin} />
              </PublicRoute>
            } 
          />

          {/* Rotas Protegidas */}
          <Route 
            path="/dashboard" 
            element={
              <ProtectedRoute>
                <DashboardPage onLogout={handleLogout} />
              </ProtectedRoute>
            } 
          />

          {/* Rotas Funcionais Protegidas */}
          <Route 
            path="/meus-dados" 
            element={
              <ProtectedRoute>
                <MeusDadosPage />
              </ProtectedRoute>
            } 
          />

          <Route 
            path="/receber-encomenda" 
            element={
              <ProtectedRoute>
                <ReceberEncomendaPage />
              </ProtectedRoute>
            } 
          />

          <Route 
            path="/retirar-encomenda" 
            element={
              <ProtectedRoute>
                <RetirarEncomendaPage />
              </ProtectedRoute>
            } 
          />

          <Route 
            path="/confirmar-notificacoes" 
            element={
              <ProtectedRoute>
                <ConfirmarNotificacoesPage />
              </ProtectedRoute>
            } 
          />

          {/* Rota para qualquer caminho não encontrado */}
          <Route 
            path="*" 
            element={
              <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="text-center">
                  <h1 className="text-2xl font-bold mb-4">Página Não Encontrada</h1>
                  <p className="text-gray-600 mb-4">A página que você procura não existe.</p>
                  <a 
                    href="/"
                    className="text-primary-600 hover:text-primary-800 underline"
                  >
                    Voltar para Home
                  </a>
                </div>
              </div>
            } 
          />
        </Routes>
      </div>
    </Router>
  )
}

export default App
