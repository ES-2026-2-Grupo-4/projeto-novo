import { Navigate, useLocation, Outlet } from 'react-router-dom';
import { useAuth } from '../context/useAuth.ts';
import type { UserRole } from '../types/auth.ts';

interface ProtectedRouteProps {
  allowedRoles?: UserRole[];
  children?: React.ReactNode;
}

export function ProtectedRoute({ allowedRoles, children }: ProtectedRouteProps) {
  const { isAuthenticated, isLoading, user } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="auth-loading-screen">
        <div className="auth-spinner"></div>
        <p>Carregando sessão...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    // Redireciona para o login mantendo o caminho tentado para redirecionar de volta após login
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && user && !allowedRoles.includes(user.perfil)) {
    // Usuário autenticado, mas sem o perfil necessário
    return <Navigate to="/unauthorized" replace />;
  }

  return children ? <>{children}</> : <Outlet />;
}
