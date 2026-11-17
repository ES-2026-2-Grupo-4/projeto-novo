export type Modalidade = "FUTEBOL" | "FUTSAL" | "VOLEI" | "BASQUETE";

export type FormatoDisputa = "PONTOS_CORRIDOS" | "MATA_MATA" | "FASE_DE_GRUPOS";

export type StatusCampeonato =
  | "RASCUNHO"
  | "INSCRICOES_ABERTAS"
  | "INSCRICOES_ENCERRADAS"
  | "EM_ANDAMENTO"
  | "FINALIZADO";

export type TipoLocal = "QUADRA" | "GINASIO" | "CAMPO";

export interface LocalResponse {
  id: string;
  nome: string;
  endereco?: string | null;
  tipo: TipoLocal;
  criadoEm: string;
}

export interface LocalRequest {
  nome: string;
  endereco?: string;
  tipo: TipoLocal;
}

export interface CampeonatoResponse {
  id: string;
  nome: string;
  descricao?: string | null;
  modalidade: Modalidade;
  formato: FormatoDisputa;
  quantidadeGrupos?: number | null;
  maxTimes: number;
  vagasOcupadas: number;
  status: StatusCampeonato;
  dataInicio: string; // formato ISO YYYY-MM-DD (LocalDate)
  dataFim: string; // formato ISO YYYY-MM-DD (LocalDate)
  organizadorId: string;
  locais: LocalResponse[];
  criadoEm: string;
}

export interface CampeonatoRequest {
  nome: string;
  descricao?: string;
  modalidade: Modalidade;
  formato: FormatoDisputa;
  quantidadeGrupos?: number | null;
  maxTimes: number;
  dataInicio: string;
  dataFim: string;
}

export interface LocaisRequest {
  localIds: string[];
}

export const MODALIDADE_LABELS: Record<Modalidade, string> = {
  FUTEBOL: "Futebol",
  FUTSAL: "Futsal",
  VOLEI: "Vôlei",
  BASQUETE: "Basquete",
};

export const FORMATO_DISPUTA_LABELS: Record<FormatoDisputa, string> = {
  PONTOS_CORRIDOS: "Pontos Corridos",
  MATA_MATA: "Mata-Mata",
  FASE_DE_GRUPOS: "Fase de Grupos",
};

export const STATUS_CAMPEONATO_LABELS: Record<StatusCampeonato, string> = {
  RASCUNHO: "Rascunho",
  INSCRICOES_ABERTAS: "Inscrições Abertas",
  INSCRICOES_ENCERRADAS: "Inscrições Encerradas",
  EM_ANDAMENTO: "Em Andamento",
  FINALIZADO: "Finalizado",
};

export const TIPO_LOCAL_LABELS: Record<TipoLocal, string> = {
  QUADRA: "Quadra",
  GINASIO: "Ginásio",
  CAMPO: "Campo",
};
