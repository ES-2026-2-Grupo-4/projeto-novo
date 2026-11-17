import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext.tsx";
import { ProtectedRoute } from "./routes/ProtectedRoute.tsx";
import { PublicRoute } from "./routes/PublicRoute.tsx";
import { AppLayout } from "./components/layout/AppLayout.tsx";

// Páginas de Autenticação
import { LoginPage } from "./pages/auth/LoginPage.tsx";
import { RegisterPage } from "./pages/auth/RegisterPage.tsx";
import { ForgotPasswordPage } from "./pages/auth/ForgotPasswordPage.tsx";
import { ResetPasswordPage } from "./pages/auth/ResetPasswordPage.tsx";
import { VerifyAccountPage } from "./pages/auth/VerifyAccountPage.tsx";

// Páginas Restritas / Protegidas
import { DashboardPage } from "./pages/dashboard/DashboardPage.tsx";
import { ProfilePage } from "./pages/profile/ProfilePage.tsx";

// Páginas de Erro
import { UnauthorizedPage } from "./pages/UnauthorizedPage.tsx";
import { NotFoundPage } from "./pages/NotFoundPage.tsx";

// Páginas de Campeonatos
import { ChampionshipsListPage } from "./pages/championships/ChampionshipsListPage.tsx";
import { ChampionshipFormPage } from "./pages/championships/ChampionshipFormPage.tsx";

export function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Rotas Públicas (acessíveis quando deslogado; redirecionam se autenticado) */}
          <Route element={<PublicRoute />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/cadastro" element={<RegisterPage />} />
            <Route path="/register" element={<Navigate to="/cadastro" replace />} />
            <Route path="/esqueci-senha" element={<ForgotPasswordPage />} />
            <Route path="/forgot-password" element={<Navigate to="/esqueci-senha" replace />} />
            <Route path="/redefinir-senha" element={<ResetPasswordPage />} />
            <Route path="/redefinir-senha/:token" element={<ResetPasswordPage />} />
            <Route path="/reset-password/:token" element={<ResetPasswordPage />} />
            <Route path="/verificar-conta" element={<VerifyAccountPage />} />
          </Route>

          {/* Rotas Protegidas (somente usuários autenticados) */}
          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/perfil" element={<ProfilePage />} />
              <Route path="/campeonatos" element={<ChampionshipsListPage />} />
              <Route path="/campeonatos/novo" element={<ChampionshipFormPage />} />
              <Route path="/campeonatos/:id/editar" element={<ChampionshipFormPage />} />
            </Route>
          </Route>

          {/* Páginas Informativas e de Erro */}
          <Route path="/unauthorized" element={<UnauthorizedPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
