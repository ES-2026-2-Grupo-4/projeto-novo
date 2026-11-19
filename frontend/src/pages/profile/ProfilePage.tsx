import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/useAuth.ts';
import { Card } from '../../components/common/Card.tsx';
import { Button } from '../../components/common/Button.tsx';
import { Alert } from '../../components/common/Alert.tsx';
import {
  UserIcon,
  MailIcon,
  ShieldIcon,
  CheckCircleIcon,
  AlertCircleIcon,
  ArrowLeftIcon,
} from '../../components/common/Icons.tsx';

export function ProfilePage() {
  const { user, token, refreshUser } = useAuth();
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [feedback, setFeedback] = useState<string | null>(null);

  const handleRefresh = async () => {
    setIsRefreshing(true);
    setFeedback(null);
    try {
      await refreshUser();
      setFeedback('Perfil atualizado com sucesso a partir da API!');
    } catch {
      setFeedback('Não foi possível atualizar os dados agora.');
    } finally {
      setIsRefreshing(false);
    }
  };

  return (
    <div className="profile-container">
      <div className="profile-header-nav">
        <Link to="/dashboard" className="auth-back-link">
          <ArrowLeftIcon /> Voltar ao Dashboard
        </Link>
      </div>

      <div className="profile-page-header">
        <h1>Meu Perfil</h1>
        <p>Gerencie seus dados e confira os detalhes da sua conta na Arena UFCG</p>
      </div>

      {feedback && (
        <Alert variant="info" onClose={() => setFeedback(null)} className="mb-4">
          {feedback}
        </Alert>
      )}

      <div className="profile-layout-grid">
        <Card className="profile-summary-card">
          <div className="profile-avatar-large">
            {user?.nome ? user.nome.charAt(0).toUpperCase() : 'U'}
          </div>
          <h2 className="profile-user-name">{user?.nome}</h2>
          <span className="profile-role-badge">
            <ShieldIcon className="role-icon" /> {user?.perfil}
          </span>
          <p className="profile-email-sub">{user?.email}</p>

          <div className="profile-status-box">
            {user?.verificado ? (
              <div className="badge-verified">
                <CheckCircleIcon /> Conta Verificada
              </div>
            ) : (
              <div className="badge-unverified">
                <AlertCircleIcon /> E-mail não verificado
                <Link to="/verificar-conta" className="btn btn-sm btn-outline mt-2">
                  Verificar agora
                </Link>
              </div>
            )}
          </div>
        </Card>

        <Card className="profile-details-card">
          <h3 className="details-title">Informações da Conta</h3>

          <div className="profile-fields-list">
            <div className="profile-field">
              <span className="field-icon">
                <UserIcon />
              </span>
              <div className="field-info">
                <label>Nome Completo</label>
                <div className="field-value">{user?.nome}</div>
              </div>
            </div>

            <div className="profile-field">
              <span className="field-icon">
                <MailIcon />
              </span>
              <div className="field-info">
                <label>Endereço de E-mail</label>
                <div className="field-value">{user?.email}</div>
              </div>
            </div>

            <div className="profile-field">
              <span className="field-icon">
                <ShieldIcon />
              </span>
              <div className="field-info">
                <label>Perfil no Sistema</label>
                <div className="field-value">{user?.perfil}</div>
              </div>
            </div>

            <div className="profile-field">
              <span className="field-icon">
                <CheckCircleIcon />
              </span>
              <div className="field-info">
                <label>Identificador de Usuário (UUID)</label>
                <div className="field-value mono-text">{user?.id || '—'}</div>
              </div>
            </div>

            <div className="profile-field">
              <span className="field-icon">
                <ShieldIcon />
              </span>
              <div className="field-info">
                <label>Token de Sessão JWT / Bearer</label>
                <div className="field-value mono-text token-snippet">
                  {token ? `${token.slice(0, 24)}... (Ativo)` : 'Não disponível'}
                </div>
              </div>
            </div>
          </div>

          <div className="profile-actions-footer">
            <Button
              variant="outline"
              size="md"
              isLoading={isRefreshing}
              onClick={handleRefresh}
            >
              Recarregar dados da API
            </Button>
            <Link to="/esqueci-senha" className="btn btn-ghost btn-md">
              Alterar senha
            </Link>
          </div>
        </Card>
      </div>
    </div>
  );
}
