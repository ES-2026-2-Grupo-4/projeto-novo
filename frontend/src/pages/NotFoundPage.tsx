import { Link } from 'react-router-dom';
import { useAuth } from '../context/useAuth.ts';

export function NotFoundPage() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="error-page-container">
      <div className="error-page-card">
        <h1 className="error-code">404</h1>
        <h2>Página não encontrada</h2>
        <p>A página que você está procurando não existe ou foi movida.</p>
        <div className="error-actions">
          <Link
            to={isAuthenticated ? '/dashboard' : '/login'}
            className="btn btn-primary"
          >
            {isAuthenticated ? 'Ir para o Dashboard' : 'Ir para o Login'}
          </Link>
        </div>
      </div>
    </div>
  );
}
