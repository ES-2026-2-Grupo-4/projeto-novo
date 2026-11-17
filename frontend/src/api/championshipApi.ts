import { apiClient } from "./client.ts";
import type { CampeonatoRequest, CampeonatoResponse, LocaisRequest } from "../types/championship.ts";

export const championshipApi = {
  listarMeus: (): Promise<CampeonatoResponse[]> => {
    return apiClient<CampeonatoResponse[]>("/championships/me", {
      method: "GET",
    });
  },

  buscarPorId: (id: string): Promise<CampeonatoResponse> => {
    return apiClient<CampeonatoResponse>(`/championships/${id}`, {
      method: "GET",
    });
  },

  criar: (data: CampeonatoRequest): Promise<CampeonatoResponse> => {
    return apiClient<CampeonatoResponse>("/championships", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  atualizar: (id: string, data: CampeonatoRequest): Promise<CampeonatoResponse> => {
    return apiClient<CampeonatoResponse>(`/championships/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },

  remover: (id: string): Promise<void> => {
    return apiClient<void>(`/championships/${id}`, {
      method: "DELETE",
    });
  },

  abrirInscricoes: (id: string): Promise<CampeonatoResponse> => {
    return apiClient<CampeonatoResponse>(`/championships/${id}/open-registrations`, {
      method: "POST",
    });
  },

  encerrarInscricoes: (id: string): Promise<CampeonatoResponse> => {
    return apiClient<CampeonatoResponse>(`/championships/${id}/close-registrations`, {
      method: "POST",
    });
  },

  definirLocais: (id: string, data: LocaisRequest): Promise<CampeonatoResponse> => {
    return apiClient<CampeonatoResponse>(`/championships/${id}/venues`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },
};
