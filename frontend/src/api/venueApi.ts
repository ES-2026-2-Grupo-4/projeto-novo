import { apiClient } from "./client.ts";
import type { LocalRequest, LocalResponse } from "../types/championship.ts";

export const venueApi = {
  listar: (): Promise<LocalResponse[]> => {
    return apiClient<LocalResponse[]>("/venues", {
      method: "GET",
    });
  },

  cadastrar: (data: LocalRequest): Promise<LocalResponse> => {
    return apiClient<LocalResponse>("/venues", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
};
