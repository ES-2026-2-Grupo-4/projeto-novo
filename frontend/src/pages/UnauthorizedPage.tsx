import { Link } from 'react-router-dom';
import { AlertCircleIcon } from '../components/common/Icons.tsx';

export function UnauthorizedPage() {
  return (
    <div className="error-page-container">
      <div className="error-page-card">
        <div className="error-icon-box error-unauthorized">
          <AlertCircleIcon />
        </div>
        <h1>403 - Acesso Negado</h1>
        <p>
          Seu perfil de usuário não possui as permissões necessárias para acessar este recurso
          ou página restrita.
        </p>
        <div className="error-actions">
          <Link to="/dashboard" className="btn btn-primary">
            Voltar ao Dashboard
          </Link>
        </div>
      </div>
    </div>
  );
}
