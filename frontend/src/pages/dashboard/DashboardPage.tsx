import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/useAuth.ts';
import { Card } from '../../components/common/Card.tsx';
import { Button } from '../../components/common/Button.tsx';
import { Alert } from '../../components/common/Alert.tsx';
import {
  TrophyIcon,
  ShieldIcon,
  CheckCircleIcon,
  AlertCircleIcon,
  UserIcon,
} from '../../components/common/Icons.tsx';

export function DashboardPage() {
  const { user, token, refreshUser } = useAuth();
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [apiFeedback, setApiFeedback] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const handleTestApiCall = async () => {
    setIsRefreshing(true);
    setApiFeedback(null);
    try {
      await refreshUser();
      setApiFeedback({
        type: 'success',
        message: 'Requisição HTTP GET /api/v1/auth/me executada com sucesso usando o token Bearer!',
      });
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Falha na requisição à API.';
      setApiFeedback({
        type: 'error',
        message: `Erro na requisição: ${msg}`,
      });
    } finally {
      setIsRefreshing(false);
    }
  };

  const getRoleBadgeClass = (role?: string) => {
    switch (role) {
      case 'ORGANIZADOR':
        return 'badge-organizador';
      case 'DIRIGENTE':
        return 'badge-dirigente';
      case 'ARBITRO':
        return 'badge-arbitro';
      case 'ESPECTADOR':
      default:
        return 'badge-espectador';
    }
  };

  const renderRoleSpecificActions = () => {
    if (!user) return null;

    switch (user.perfil) {
      case 'ORGANIZADOR':
        return (
          <div className="role-actions-grid">
            <div className="action-card">
              <span className="action-tag">Gestão</span>
              <h4>Criar Novo Campeonato</h4>
              <p>Configure modalidades, regulamentos, fases de grupos e eliminatórias.</p>
              <button className="btn btn-outline btn-sm">Novo Torneio</button>
            </div>
            <div className="action-card">
              <span className="action-tag">Inscrições</span>
              <h4>Aprovar Inscrições</h4>
              <p>Avalie as equipes e documentos submetidos pelos dirigentes esportivos.</p>
              <button className="btn btn-outline btn-sm">Ver Inscrições</button>
            </div>
            <div className="action-card">
              <span className="action-tag">Tabela</span>
              <h4>Gerar Calendário</h4>
              <p>Distribua confrontos, horários e quadras na Arena UFCG.</p>
              <button className="btn btn-outline btn-sm">Gerar Tabela</button>
            </div>
          </div>
        );
      case 'DIRIGENTE':
        return (
          <div className="role-actions-grid">
            <div className="action-card">
              <span className="action-tag">Equipes</span>
              <h4>Inscrever Equipe</h4>
              <p>Submeta seu time para os campeonatos com inscrições abertas.</p>
              <button className="btn btn-outline btn-sm">Nova Inscrição</button>
            </div>
            <div className="action-card">
              <span className="action-tag">Atletas</span>
              <h4>Gerenciar Elenco</h4>
              <p>Cadastre atletas, números de camisa e posições da sua equipe.</p>
              <button className="btn btn-outline btn-sm">Meus Atletas</button>
            </div>
          </div>
        );
      case 'ARBITRO':
        return (
          <div className="role-actions-grid">
            <div className="action-card">
              <span className="action-tag">Partidas</span>
              <h4>Minhas Escalações</h4>
              <p>Consulte as partidas onde você foi escalado como árbitro ou mesário.</p>
              <button className="btn btn-outline btn-sm">Ver Jogos</button>
            </div>
            <div className="action-card">
              <span className="action-tag">Súmulas</span>
              <h4>Lançar Resultados</h4>
              <p>Preencha os placares, gols, cartões e observações das partidas.</p>
              <button className="btn btn-outline btn-sm">Preencher Súmula</button>
            </div>
          </div>
        );
      case 'ESPECTADOR':
      default:
        return (
          <div className="role-actions-grid">
            <div className="action-card">
              <span className="action-tag">Competições</span>
              <h4>Tabelas e Jogos</h4>
              <p>Acompanhe a classificação, artilharia e resultados dos campeonatos.</p>
              <button className="btn btn-outline btn-sm">Explorar Jogos</button>
            </div>
            <div className="action-card">
              <span className="action-tag">Ao Vivo</span>
              <h4>Próximas Partidas</h4>
              <p>Veja os jogos do dia nos ginásios e campos do campus da UFCG.</p>
              <button className="btn btn-outline btn-sm">Ver Calendário</button>
            </div>
          </div>
        );
    }
  };

  return (
    <div className="dashboard-content-wrapper">
      <div className="dashboard-hero-banner">
        <div className="hero-text-col">
          <span className="hero-pill">
            <ShieldIcon className="pill-icon" /> Rota Protegida (Área Autenticada)
          </span>
          <h1 className="hero-greeting">
            Olá, <strong>{user?.nome || 'Usuário'}</strong>!
          </h1>
          <p className="hero-description">
            Bem-vindo à plataforma de competições esportivas <strong>Arena UFCG</strong>. Seu
            acesso está ativo com o perfil <strong>{user?.perfil}</strong>.
          </p>
        </div>
        <div className="hero-badge-col">
          <div className="trophy-display">
            <TrophyIcon className="trophy-display-icon" />
          </div>
        </div>
      </div>

      {apiFeedback && (
        <Alert
          variant={apiFeedback.type}
          onClose={() => setApiFeedback(null)}
          className="mb-4"
        >
          {apiFeedback.message}
        </Alert>
      )}

      {user && !user.verificado && (
        <Alert variant="warning" className="mb-4">
          <div className="unverified-banner">
            <div>
              <strong>Sua conta ainda não foi verificada por e-mail.</strong>
              <p>
                Insira o código de 6 dígitos enviado para <strong>{user.email}</strong> para
                liberar todos os recursos.
              </p>
            </div>
            <Link to="/verificar-conta" className="btn btn-sm btn-outline mt-2">
              Verificar Conta Agora
            </Link>
          </div>
        </Alert>
      )}

      <div className="dashboard-grid-2col">
        {/* Card: Dados da Sessão Autenticada */}
        <Card className="dashboard-card">
          <div className="card-header-styled">
            <div className="card-header-icon">
              <UserIcon />
            </div>
            <div>
              <h3 className="card-title">Dados da Sessão</h3>
              <p className="card-subtitle">Informações recuperadas da API REST</p>
            </div>
          </div>

          <div className="session-details-list">
            <div className="detail-item">
              <span className="detail-label">Nome:</span>
              <span className="detail-val">{user?.nome}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">E-mail:</span>
              <span className="detail-val">{user?.email}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Perfil / Role:</span>
              <span className={`detail-val badge ${getRoleBadgeClass(user?.perfil)}`}>
                {user?.perfil}
              </span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Status da Conta:</span>
              <span className="detail-val status-val">
                {user?.verificado ? (
                  <span className="verified-status is-verified">
                    <CheckCircleIcon /> E-mail Verificado
                  </span>
                ) : (
                  <span className="verified-status is-unverified">
                    <AlertCircleIcon /> Pendente de Verificação
                  </span>
                )}
              </span>
            </div>
            <div className="detail-item">
              <span className="detail-label">ID do Usuário (UUID):</span>
              <span className="detail-val mono-text">{user?.id || '—'}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Token de Acesso (Bearer):</span>
              <span className="detail-val mono-text token-snippet">
                {token ? `${token.slice(0, 16)}...${token.slice(-8)}` : 'Nenhum'}
              </span>
            </div>
          </div>

          <div className="card-action-bar">
            <Button
              variant="outline"
              size="sm"
              isLoading={isRefreshing}
              onClick={handleTestApiCall}
            >
              Testar Requisição HTTP (/api/v1/auth/me)
            </Button>
          </div>
        </Card>

        {/* Card: Arquitetura e Proteção */}
        <Card className="dashboard-card">
          <div className="card-header-styled">
            <div className="card-header-icon">
              <ShieldIcon />
            </div>
            <div>
              <h3 className="card-title">Proteção de Rotas Ativa</h3>
              <p className="card-subtitle">Como este sistema protege suas páginas</p>
            </div>
          </div>

          <div className="protection-explanation">
            <div className="security-feature">
              <div className="feature-number">1</div>
              <div>
                <strong>Guarda de Rotas (ProtectedRoute)</strong>
                <p>
                  Intercepta tentativas de acesso a páginas restritas. Se o token não existir
                  ou for inválido, redireciona para <code>/login</code> preservando o histórico.
                </p>
              </div>
            </div>

            <div className="security-feature">
              <div className="feature-number">2</div>
              <div>
                <strong>Injeção Automática de Headers HTTP</strong>
                <p>
                  O cliente HTTP adiciona automaticamente o cabeçalho{' '}
                  <code>Authorization: Bearer &lt;token&gt;</code> em todas as requisições autenticadas.
                </p>
              </div>
            </div>

            <div className="security-feature">
              <div className="feature-number">3</div>
              <div>
                <strong>Tratamento de Sessão Expirada (401)</strong>
                <p>
                  Se o backend retornar código HTTP 401, o estado da sessão é automaticamente limpo
                  e o usuário é direcionado para a tela de autenticação.
                </p>
              </div>
            </div>
          </div>
        </Card>
      </div>

      {/* Ações específicas por perfil */}
      <div className="role-actions-section">
        <h3 className="section-title">
          Painel de Ações &bull; {user?.perfil}
        </h3>
        {renderRoleSpecificActions()}
      </div>
    </div>
  );
}
