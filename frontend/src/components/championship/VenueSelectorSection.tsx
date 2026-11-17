import { useState, type FormEvent } from "react";
import { venueApi } from "../../api/venueApi.ts";
import { TIPO_LOCAL_LABELS, type LocalResponse, type TipoLocal } from "../../types/championship.ts";
import { Input } from "../common/Input.tsx";
import { Select } from "../common/Select.tsx";
import { Button } from "../common/Button.tsx";
import { Alert } from "../common/Alert.tsx";
import { MapPinIcon, CheckCircleIcon } from "../common/Icons.tsx";

interface VenueSelectorSectionProps {
  availableVenues: LocalResponse[];
  selectedVenueIds: string[];
  onChangeSelected: (ids: string[]) => void;
  onVenueCreated: (newVenue: LocalResponse) => void;
  disabled?: boolean;
  disabledReason?: string;
}

const TIPO_LOCAL_OPTIONS = [
  { value: "QUADRA", label: "Quadra" },
  { value: "GINASIO", label: "Ginásio" },
  { value: "CAMPO", label: "Campo" },
];

export function VenueSelectorSection({
  availableVenues,
  selectedVenueIds,
  onChangeSelected,
  onVenueCreated,
  disabled = false,
  disabledReason,
}: VenueSelectorSectionProps) {
  const [isCreatingNew, setIsCreatingNew] = useState(false);
  const [nome, setNome] = useState("");
  const [endereco, setEndereco] = useState("");
  const [tipo, setTipo] = useState<TipoLocal>("QUADRA");
  const [isSavingVenue, setIsSavingVenue] = useState(false);
  const [venueError, setVenueError] = useState<string | null>(null);

  const toggleVenue = (id: string) => {
    if (disabled) return;
    if (selectedVenueIds.includes(id)) {
      onChangeSelected(selectedVenueIds.filter((item) => item !== id));
    } else {
      onChangeSelected([...selectedVenueIds, id]);
    }
  };

  const handleCreateVenue = async (e: FormEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setVenueError(null);

    if (!nome.trim()) {
      setVenueError("Informe o nome do local.");
      return;
    }

    setIsSavingVenue(true);
    try {
      const created = await venueApi.cadastrar({
        nome: nome.trim(),
        endereco: endereco.trim() || undefined,
        tipo,
      });

      onVenueCreated(created);
      onChangeSelected([...selectedVenueIds, created.id]);

      setNome("");
      setEndereco("");
      setTipo("QUADRA");
      setIsCreatingNew(false);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Não foi possível cadastrar o local.";
      setVenueError(msg);
    } finally {
      setIsSavingVenue(false);
    }
  };

  return (
    <div className="venue-selector-section">
      <div className="venue-selector-header">
        <div>
          <h4 className="venue-selector-title">
            <MapPinIcon /> Locais das Partidas
          </h4>
          <p className="venue-selector-subtitle">
            Selecione as quadras, ginásios ou campos onde o campeonato acontecerá. Necessário ao menos 1 local para
            abrir inscrições.
          </p>
        </div>

        {!disabled && (
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={() => {
              setIsCreatingNew((prev) => !prev);
              setVenueError(null);
            }}
          >
            {isCreatingNew ? "Cancelar novo local" : "+ Cadastrar novo local"}
          </Button>
        )}
      </div>

      {disabled && disabledReason && (
        <Alert variant="warning" className="mb-3">
          {disabledReason}
        </Alert>
      )}

      {isCreatingNew && !disabled && (
        <div className="venue-inline-form-box">
          <h5 className="venue-inline-form-title">Novo Local Esportivo</h5>

          {venueError && (
            <Alert variant="error" onClose={() => setVenueError(null)} className="mb-3">
              {venueError}
            </Alert>
          )}

          <div className="venue-inline-grid">
            <Input
              label="Nome do local"
              placeholder="Ex: Ginásio Poliesportivo UFCG"
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              maxLength={120}
              required
            />

            <Select
              label="Tipo de espaço"
              options={TIPO_LOCAL_OPTIONS}
              value={tipo}
              onChange={(e) => setTipo(e.target.value as TipoLocal)}
              required
            />
          </div>

          <Input
            label="Endereço / Referência (opcional)"
            placeholder="Ex: Próximo ao Centro de Extensão José Farias Nóbrega"
            value={endereco}
            onChange={(e) => setEndereco(e.target.value)}
            maxLength={255}
            className="mt-2"
          />

          <div className="venue-inline-actions">
            <Button type="button" variant="primary" size="sm" isLoading={isSavingVenue} onClick={handleCreateVenue}>
              Salvar e vincular local
            </Button>
          </div>
        </div>
      )}

      {availableVenues.length === 0 ? (
        <div className="venue-empty-state">
          <p>Nenhum local cadastrado na plataforma ainda.</p>
          {!disabled && !isCreatingNew && (
            <Button type="button" variant="outline" size="sm" onClick={() => setIsCreatingNew(true)}>
              Cadastrar primeiro local
            </Button>
          )}
        </div>
      ) : (
        <div className="venue-cards-grid">
          {availableVenues.map((venue) => {
            const isSelected = selectedVenueIds.includes(venue.id);
            return (
              <button
                key={venue.id}
                type="button"
                disabled={disabled}
                onClick={() => toggleVenue(venue.id)}
                className={`venue-select-card ${isSelected ? "is-selected" : ""} ${disabled ? "is-disabled" : ""}`}
              >
                <div className="venue-card-top">
                  <span className="venue-type-badge">{TIPO_LOCAL_LABELS[venue.tipo] || venue.tipo}</span>
                  {isSelected && (
                    <span className="venue-selected-check">
                      <CheckCircleIcon />
                    </span>
                  )}
                </div>
                <strong className="venue-card-name">{venue.nome}</strong>
                <span className="venue-card-address">{venue.endereco || "Campus UFCG"}</span>
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
