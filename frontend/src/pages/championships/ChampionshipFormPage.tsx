import { useEffect, useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { championshipApi } from "../../api/championshipApi.ts";
import { venueApi } from "../../api/venueApi.ts";
import {
  STATUS_CAMPEONATO_LABELS,
  type CampeonatoRequest,
  type FormatoDisputa,
  type LocalResponse,
  type Modalidade,
  type StatusCampeonato,
} from "../../types/championship.ts";
import { Card } from "../../components/common/Card.tsx";
import { Input } from "../../components/common/Input.tsx";
import { Select } from "../../components/common/Select.tsx";
import { Button } from "../../components/common/Button.tsx";
import { Alert } from "../../components/common/Alert.tsx";
import { VenueSelectorSection } from "../../components/championship/VenueSelectorSection.tsx";
import { ArrowLeftIcon } from "../../components/common/Icons.tsx";

const MODALIDADE_OPTIONS = [
  { value: "FUTSAL", label: "Futsal" },
  { value: "FUTEBOL", label: "Futebol" },
  { value: "VOLEI", label: "Vôlei" },
  { value: "BASQUETE", label: "Basquete" },
];

const FORMATO_OPTIONS = [
  { value: "FASE_DE_GRUPOS", label: "Fase de Grupos" },
  { value: "MATA_MATA", label: "Mata-Mata" },
  { value: "PONTOS_CORRIDOS", label: "Pontos Corridos" },
];

export function ChampionshipFormPage() {
  const { id } = useParams<{ id: string }>();
  const isEditing = Boolean(id);
  const navigate = useNavigate();

  const [isLoadingData, setIsLoadingData] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  // Dados do campeonato
  const [nome, setNome] = useState("");
  const [descricao, setDescricao] = useState("");
  const [modalidade, setModalidade] = useState<Modalidade>("FUTSAL");
  const [formato, setFormato] = useState<FormatoDisputa>("FASE_DE_GRUPOS");
  const [quantidadeGrupos, setQuantidadeGrupos] = useState<string>("2");
  const [maxTimes, setMaxTimes] = useState<string>("8");
  const [dataInicio, setDataInicio] = useState("");
  const [dataFim, setDataFim] = useState("");
  const [status, setStatus] = useState<StatusCampeonato>("RASCUNHO");
  const [vagasOcupadas, setVagasOcupadas] = useState<number>(0);

  // Locais
  const [availableVenues, setAvailableVenues] = useState<LocalResponse[]>([]);
  const [selectedVenueIds, setSelectedVenueIds] = useState<string[]>([]);

  useEffect(() => {
    let active = true;

    const loadInitialData = async () => {
      setIsLoadingData(true);
      setErrorMessage(null);
      try {
        const venuesPromise = venueApi.listar();
        const championshipPromise = id ? championshipApi.buscarPorId(id) : Promise.resolve(null);

        const [venues, championship] = await Promise.all([venuesPromise, championshipPromise]);

        if (!active) return;

        setAvailableVenues(venues);

        if (championship) {
          setNome(championship.nome);
          setDescricao(championship.descricao || "");
          setModalidade(championship.modalidade);
          setFormato(championship.formato);
          setQuantidadeGrupos(championship.quantidadeGrupos ? String(championship.quantidadeGrupos) : "2");
          setMaxTimes(String(championship.maxTimes));
          setDataInicio(championship.dataInicio);
          setDataFim(championship.dataFim);
          setStatus(championship.status);
          setVagasOcupadas(championship.vagasOcupadas);
          setSelectedVenueIds(championship.locais.map((l) => l.id));
        }
      } catch (err: unknown) {
        if (!active) return;
        const msg = err instanceof Error ? err.message : "Não foi possível carregar os dados do formulário.";
        setErrorMessage(msg);
      } finally {
        if (active) setIsLoadingData(false);
      }
    };

    void loadInitialData();

    return () => {
      active = false;
    };
  }, [id]);

  // Regras de bloqueio do domínio (Campeonato.java)
  const hasStarted = status === "EM_ANDAMENTO" || status === "FINALIZADO";
  const isNotDraft = isEditing && status !== "RASCUNHO";

  const validate = (): boolean => {
    const errors: Record<string, string> = {};
    const parsedMaxTimes = Number(maxTimes);
    const parsedGroups = Number(quantidadeGrupos);

    if (!nome.trim()) {
      errors.nome = "O nome do campeonato é obrigatório.";
    } else if (nome.trim().length > 120) {
      errors.nome = "O nome deve ter no máximo 120 caracteres.";
    }

    if (descricao.length > 1000) {
      errors.descricao = "A descrição deve ter no máximo 1000 caracteres.";
    }

    if (!Number.isInteger(parsedMaxTimes) || parsedMaxTimes < 2 || parsedMaxTimes > 64) {
      errors.maxTimes = "Informe um número entre 2 e 64 equipes.";
    } else if (isEditing && parsedMaxTimes < vagasOcupadas) {
      errors.maxTimes = `Não pode ser menor que as vagas já ocupadas (${vagasOcupadas}).`;
    }

    if (formato === "FASE_DE_GRUPOS") {
      if (!Number.isInteger(parsedGroups) || parsedGroups < 1 || parsedGroups > parsedMaxTimes) {
        errors.quantidadeGrupos = `Informe entre 1 e ${parsedMaxTimes || 64} grupos.`;
      }
    }

    if (!dataInicio) {
      errors.dataInicio = "A data de início é obrigatória.";
    }

    if (!dataFim) {
      errors.dataFim = "A data de término é obrigatória.";
    }

    if (dataInicio && dataFim && dataFim < dataInicio) {
      errors.dataFim = "A data de término não pode ser anterior à data de início.";
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!validate()) {
      return;
    }

    setIsSubmitting(true);

    const payload: CampeonatoRequest = {
      nome: nome.trim(),
      descricao: descricao.trim() || undefined,
      modalidade,
      formato,
      quantidadeGrupos: formato === "FASE_DE_GRUPOS" ? Number(quantidadeGrupos) : null,
      maxTimes: Number(maxTimes),
      dataInicio,
      dataFim,
    };

    try {
      if (isEditing && id) {
        await championshipApi.atualizar(id, payload);
        if (!hasStarted) {
          await championshipApi.definirLocais(id, {
            localIds: selectedVenueIds,
          });
        }
      } else {
        const created = await championshipApi.criar(payload);
        if (selectedVenueIds.length > 0) {
          await championshipApi.definirLocais(created.id, {
            localIds: selectedVenueIds,
          });
        }
      }

      navigate("/campeonatos");
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Ocorreu um erro ao salvar o campeonato.";
      setErrorMessage(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  const clearFieldError = (field: string) => {
    if (fieldErrors[field]) {
      setFieldErrors((prev) => {
        const copy = { ...prev };
        delete copy[field];
        return copy;
      });
    }
  };

  if (isLoadingData) {
    return (
      <div className="championships-page-wrapper">
        <Card className="championships-empty-card">
          <p>Carregando dados do campeonato...</p>
        </Card>
      </div>
    );
  }

  return (
    <div className="championships-page-wrapper">
      <div className="page-header-row">
        <div>
          <Link to="/campeonatos" className="back-inline-link">
            <ArrowLeftIcon /> Voltar para Meus Campeonatos
          </Link>
          <h1 className="page-main-title mt-2">{isEditing ? "Editar Campeonato" : "Novo Campeonato"}</h1>
          <p className="page-main-subtitle">
            {isEditing
              ? `Status atual: ${STATUS_CAMPEONATO_LABELS[status] || status}`
              : "Configure as regras da competição e associe os locais de disputa."}
          </p>
        </div>
      </div>

      {errorMessage && (
        <Alert variant="error" onClose={() => setErrorMessage(null)} className="mb-4">
          {errorMessage}
        </Alert>
      )}

      {hasStarted && (
        <Alert variant="warning" className="mb-4">
          Este campeonato já iniciou ou foi finalizado. Apenas o <strong>nome</strong> e a <strong>descrição</strong>{" "}
          podem ser alterados.
        </Alert>
      )}

      {!hasStarted && isNotDraft && (
        <Alert variant="warning" className="mb-4">
          As inscrições já foram abertas. <strong>Modalidade</strong> e <strong>formato de disputa</strong> não podem
          mais ser alterados.
        </Alert>
      )}

      <Card className="championship-form-card">
        <form onSubmit={handleSubmit} noValidate className="championship-form">
          <div className="form-section-block">
            <h3 className="form-section-title">1. Informações Gerais</h3>

            <Input
              label="Nome do campeonato"
              placeholder="Ex: Copa Computação UFCG 2026.2"
              value={nome}
              onChange={(e) => {
                setNome(e.target.value);
                clearFieldError("nome");
              }}
              error={fieldErrors.nome}
              maxLength={120}
              required
            />

            <div className={`input-group mt-3 ${fieldErrors.descricao ? "has-error" : ""}`}>
              <label htmlFor="camp-descricao" className="input-label">
                Descrição e regulamento resumido (opcional)
              </label>
              <textarea
                id="camp-descricao"
                className="input-field textarea-field"
                rows={3}
                maxLength={1000}
                placeholder="Informe detalhes sobre premiação, regras específicas ou público-alvo..."
                value={descricao}
                onChange={(e) => {
                  setDescricao(e.target.value);
                  clearFieldError("descricao");
                }}
              />
              {fieldErrors.descricao && (
                <p className="input-error-msg" role="alert">
                  {fieldErrors.descricao}
                </p>
              )}
            </div>
          </div>

          <div className="form-section-block">
            <h3 className="form-section-title">2. Formato e Vagas</h3>

            <div className="form-grid-2">
              <Select
                label="Modalidade"
                options={MODALIDADE_OPTIONS}
                value={modalidade}
                disabled={isNotDraft}
                onChange={(e) => setModalidade(e.target.value as Modalidade)}
                required
              />

              <Select
                label="Formato de disputa"
                options={FORMATO_OPTIONS}
                value={formato}
                disabled={isNotDraft}
                onChange={(e) => setFormato(e.target.value as FormatoDisputa)}
                required
              />
            </div>

            <div className="form-grid-2 mt-3">
              <Input
                label="Número máximo de equipes"
                type="number"
                min={Math.max(2, vagasOcupadas)}
                max={64}
                value={maxTimes}
                disabled={hasStarted}
                onChange={(e) => {
                  setMaxTimes(e.target.value);
                  clearFieldError("maxTimes");
                }}
                error={fieldErrors.maxTimes}
                helperText="Mínimo 2 e máximo 64 equipes"
                required
              />

              {formato === "FASE_DE_GRUPOS" && (
                <Input
                  label="Quantidade de grupos"
                  type="number"
                  min={1}
                  max={Number(maxTimes) || 64}
                  value={quantidadeGrupos}
                  disabled={hasStarted}
                  onChange={(e) => {
                    setQuantidadeGrupos(e.target.value);
                    clearFieldError("quantidadeGrupos");
                  }}
                  error={fieldErrors.quantidadeGrupos}
                  required
                />
              )}
            </div>
          </div>

          <div className="form-section-block">
            <h3 className="form-section-title">3. Cronograma</h3>

            <div className="form-grid-2">
              <Input
                label="Data de início"
                type="date"
                value={dataInicio}
                disabled={hasStarted}
                onChange={(e) => {
                  setDataInicio(e.target.value);
                  clearFieldError("dataInicio");
                }}
                error={fieldErrors.dataInicio}
                required
              />

              <Input
                label="Data de término"
                type="date"
                value={dataFim}
                disabled={hasStarted}
                onChange={(e) => {
                  setDataFim(e.target.value);
                  clearFieldError("dataFim");
                }}
                error={fieldErrors.dataFim}
                required
              />
            </div>
          </div>

          <div className="form-section-block">
            <VenueSelectorSection
              availableVenues={availableVenues}
              selectedVenueIds={selectedVenueIds}
              onChangeSelected={setSelectedVenueIds}
              onVenueCreated={(newVenue) => setAvailableVenues((prev) => [...prev, newVenue])}
              disabled={hasStarted}
              disabledReason="Os locais não podem ser alterados depois que o campeonato já iniciou."
            />
          </div>

          <div className="championship-form-footer">
            <Link to="/campeonatos" className="btn btn-outline">
              Cancelar
            </Link>
            <Button type="submit" variant="primary" isLoading={isSubmitting}>
              {isEditing ? "Salvar Alterações" : "Criar Campeonato"}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}
