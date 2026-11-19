import type { ApiErrorResponse } from '../types/auth.ts';

const TOKEN_KEY = 'arenaufcg_auth_token';

export const getStoredToken = (): string | null => {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
};

export const setStoredToken = (token: string): void => {
  try {
    localStorage.setItem(TOKEN_KEY, token);
  } catch {
    // LocalStorage indisponível (modo privado restrito, etc.)
  }
};

export const removeStoredToken = (): void => {
  try {
    localStorage.removeItem(TOKEN_KEY);
  } catch {
    // LocalStorage indisponível
  }
};

export class ApiError extends Error {
  public status: number;
  public data?: ApiErrorResponse | unknown;

  constructor(message: string, status: number, data?: unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || '/api/v1';

interface RequestOptions extends RequestInit {
  requiresAuth?: boolean;
}

export async function apiClient<T>(
  endpoint: string,
  options: RequestOptions = {}
): Promise<T> {
  const { requiresAuth = true, headers = {}, ...restOptions } = options;

  const url = endpoint.startsWith('http')
    ? endpoint
    : `${API_BASE_URL.replace(/\/$/, '')}/${endpoint.replace(/^\//, '')}`;

  const requestHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
    Accept: 'application/json',
    ...(headers as Record<string, string>),
  };

  if (requiresAuth) {
    const token = getStoredToken();
    if (token) {
      requestHeaders['Authorization'] = `Bearer ${token}`;
    }
  }

  let response: Response;
  try {
    response = await fetch(url, {
      ...restOptions,
      headers: requestHeaders,
    });
  } catch (err: unknown) {
    const networkErrorMsg =
      'Não foi possível conectar ao servidor. Verifique se o backend está em execução.';
    throw new ApiError(networkErrorMsg, 0, err);
  }

  if (response.status === 204) {
    return {} as T;
  }

  const contentType = response.headers.get('content-type');
  const isJson = contentType && contentType.includes('application/json');
  const responseData = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    let errorMessage = 'Ocorreu um erro ao processar a requisição.';

    if (responseData && typeof responseData === 'object') {
      const errObj = responseData as Record<string, unknown>;
      if (typeof errObj.mensagem === 'string') {
        errorMessage = errObj.mensagem;
      } else if (typeof errObj.message === 'string') {
        errorMessage = errObj.message;
      } else if (typeof errObj.error === 'string') {
        errorMessage = errObj.error;
      } else if (Array.isArray(errObj.errors) && errObj.errors.length > 0) {
        errorMessage = errObj.errors.join(', ');
      }
    } else if (typeof responseData === 'string' && responseData.trim()) {
      errorMessage = responseData;
    }

    if (response.status === 401 && requiresAuth) {
      removeStoredToken();
      window.dispatchEvent(new CustomEvent('arenaufcg:unauthorized'));
    }

    throw new ApiError(errorMessage, response.status, responseData);
  }

  return responseData as T;
}
