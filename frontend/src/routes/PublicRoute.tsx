import { Navigate, useLocation, Outlet } from 'react-router-dom';
import { useAuth } from '../context/useAuth.ts';

interface PublicRouteProps {
  children?: React.ReactNode;
}

export function PublicRoute({ children }: PublicRouteProps) {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="auth-loading-screen">
        <div className="auth-spinner"></div>
      </div>
    );
  }

  if (isAuthenticated) {
    // Redireciona para o destino anterior ou para o dashboard padrão
    const state = location.state as { from?: { pathname: string } } | null;
    const origin = state?.from?.pathname || '/dashboard';
    return <Navigate to={origin} replace />;
  }

  return children ? <>{children}</> : <Outlet />;
}
