import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { championshipApi } from "../../api/championshipApi.ts";
import {
  FORMATO_DISPUTA_LABELS,
  MODALIDADE_LABELS,
  STATUS_CAMPEONATO_LABELS,
  type CampeonatoResponse,
  type StatusCampeonato,
} from "../../types/championship.ts";
import { Card } from "../../components/common/Card.tsx";
import { Button } from "../../components/common/Button.tsx";
import { Alert } from "../../components/common/Alert.tsx";
import {
  TrophyIcon,
  CalendarCheckIcon,
  MapPinIcon,
  UsersIcon,
  AlertCircleIcon,
} from "../../components/common/Icons.tsx";

function formatDateBr(isoDate: string): string {
  if (!isoDate) return "—";
  const [year, month, day] = isoDate.split("-");
  if (!year || !month || !day) return isoDate;
  return `${day}/${month}/${year}`;
}

function getStatusBadgeClass(status: StatusCampeonato): string {
  switch (status) {
    case "RASCUNHO":
      return "status-badge-rascunho";
    case "INSCRICOES_ABERTAS":
      return "status-badge-abertas";
    case "INSCRICOES_ENCERRADAS":
      return "status-badge-encerradas";
    case "EM_ANDAMENTO":
      return "status-badge-andamento";
    case "FINALIZADO":
    default:
      return "status-badge-finalizado";
  }
}

export function ChampionshipsListPage() {
  const [championships, setChampionships] = useState<CampeonatoResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const loadChampionships = async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const data = await championshipApi.listarMeus();
      setChampionships(data);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Não foi possível carregar seus campeonatos.";
      setErrorMessage(msg);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadChampionships();
  }, []);

  const handleOpenRegistrations = async (camp: CampeonatoResponse) => {
    if (camp.locais.length === 0) {
      setErrorMessage(`Associe ao menos um local ao campeonato "${camp.nome}" antes de abrir as inscrições.`);
      return;
    }

    setActionLoadingId(camp.id);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      const updated = await championshipApi.abrirInscricoes(camp.id);
      setChampionships((prev) => prev.map((item) => (item.id === camp.id ? updated : item)));
      setSuccessMessage(`Inscrições abertas para "${camp.nome}"!`);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Não foi possível abrir as inscrições.";
      setErrorMessage(msg);
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleCloseRegistrations = async (camp: CampeonatoResponse) => {
    setActionLoadingId(camp.id);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      const updated = await championshipApi.encerrarInscricoes(camp.id);
      setChampionships((prev) => prev.map((item) => (item.id === camp.id ? updated : item)));
      setSuccessMessage(`Inscrições encerradas para "${camp.nome}".`);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Não foi possível encerrar as inscrições.";
      setErrorMessage(msg);
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleDelete = async (camp: CampeonatoResponse) => {
    const confirmed = window.confirm(
      `Tem certeza que deseja remover o campeonato "${camp.nome}"? Esta ação não poderá ser desfeita.`,
    );
    if (!confirmed) return;

    setActionLoadingId(camp.id);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      await championshipApi.remover(camp.id);
      setChampionships((prev) => prev.filter((item) => item.id !== camp.id));
      setSuccessMessage(`Campeonato "${camp.nome}" removido com sucesso.`);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Não foi possível remover o campeonato.";
      setErrorMessage(msg);
    } finally {
      setActionLoadingId(null);
    }
  };

  return (
    <div className="championships-page-wrapper">
      <div className="page-header-row">
        <div>
          <h1 className="page-main-title">Meus Campeonatos</h1>
          <p className="page-main-subtitle">
            Crie, edite, vincule praças esportivas e gerencie o período de inscrições dos seus torneios.
          </p>
        </div>

        <Link to="/campeonatos/novo" className="btn btn-primary">
          + Novo Campeonato
        </Link>
      </div>

      {errorMessage && (
        <Alert variant="error" onClose={() => setErrorMessage(null)} className="mb-4">
          {errorMessage}
        </Alert>
      )}

      {successMessage && (
        <Alert variant="success" onClose={() => setSuccessMessage(null)} className="mb-4">
          {successMessage}
        </Alert>
      )}

      {isLoading ? (
        <Card className="championships-empty-card">
          <p>Carregando campeonatos...</p>
        </Card>
      ) : championships.length === 0 ? (
        <Card className="championships-empty-card">
          <div className="empty-state-icon">
            <TrophyIcon />
          </div>
          <h3>Nenhum campeonato criado ainda</h3>
          <p>
            Comece cadastrando seu primeiro torneio, definindo formato de disputa e selecionando os locais da Arena
            UFCG.
          </p>
          <Link to="/campeonatos/novo" className="btn btn-primary mt-3">
            Criar Primeiro Campeonato
          </Link>
        </Card>
      ) : (
        <div className="championships-grid">
          {championships.map((camp) => {
            const hasStarted = camp.status === "EM_ANDAMENTO" || camp.status === "FINALIZADO";
            const isBusy = actionLoadingId === camp.id;

            return (
              <Card key={camp.id} className="championship-card">
                <div className="championship-card-header">
                  <div className="championship-badges">
                    <span className="modality-badge">{MODALIDADE_LABELS[camp.modalidade] || camp.modalidade}</span>
                    <span className={`status-badge ${getStatusBadgeClass(camp.status)}`}>
                      {STATUS_CAMPEONATO_LABELS[camp.status] || camp.status}
                    </span>
                  </div>
                </div>

                <h3 className="championship-card-title">{camp.nome}</h3>

                {camp.descricao && <p className="championship-card-desc">{camp.descricao}</p>}

                <div className="championship-meta-list">
                  <div className="championship-meta-item">
                    <TrophyIcon className="meta-icon" />
                    <span>
                      <strong>Formato:</strong> {FORMATO_DISPUTA_LABELS[camp.formato] || camp.formato}
                      {camp.formato === "FASE_DE_GRUPOS" && camp.quantidadeGrupos
                        ? ` (${camp.quantidadeGrupos} grupos)`
                        : ""}
                    </span>
                  </div>

                  <div className="championship-meta-item">
                    <UsersIcon className="meta-icon" />
                    <span>
                      <strong>Equipes:</strong> {camp.vagasOcupadas} / {camp.maxTimes} vagas
                    </span>
                  </div>

                  <div className="championship-meta-item">
                    <CalendarCheckIcon className="meta-icon" />
                    <span>
                      <strong>Período:</strong> {formatDateBr(camp.dataInicio)} até {formatDateBr(camp.dataFim)}
                    </span>
                  </div>

                  <div className="championship-meta-item">
                    <MapPinIcon className="meta-icon" />
                    <span>
                      <strong>Locais:</strong>{" "}
                      {camp.locais.length > 0 ? camp.locais.map((l) => l.nome).join(", ") : "Nenhum local vinculado"}
                    </span>
                  </div>
                </div>

                {camp.status === "RASCUNHO" && camp.locais.length === 0 && (
                  <div className="championship-warning-note">
                    <AlertCircleIcon />
                    <span>Vincule ao menos 1 local na edição para poder abrir as inscrições.</span>
                  </div>
                )}

                <div className="championship-card-actions">
                  <Link to={`/campeonatos/${camp.id}/editar`} className="btn btn-outline btn-sm">
                    Editar
                  </Link>

                  {camp.status === "RASCUNHO" && (
                    <Button
                      type="button"
                      variant="primary"
                      size="sm"
                      isLoading={isBusy}
                      disabled={camp.locais.length === 0}
                      title={
                        camp.locais.length === 0
                          ? "Associe um local antes de abrir inscrições"
                          : "Abrir inscrições para equipes"
                      }
                      onClick={() => handleOpenRegistrations(camp)}
                    >
                      Abrir Inscrições
                    </Button>
                  )}

                  {camp.status === "INSCRICOES_ABERTAS" && (
                    <Button
                      type="button"
                      variant="secondary"
                      size="sm"
                      isLoading={isBusy}
                      onClick={() => handleCloseRegistrations(camp)}
                    >
                      Encerrar Inscrições
                    </Button>
                  )}

                  {!hasStarted && (
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      isLoading={isBusy}
                      className="btn-danger-ghost"
                      onClick={() => handleDelete(camp)}
                    >
                      Remover
                    </Button>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}
