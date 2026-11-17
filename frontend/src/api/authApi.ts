import { apiClient } from './client.ts';
import type {
  AuthResponse,
  LoginCredentials,
  RegisterData,
  User,
  ForgotPasswordData,
  ResetPasswordData,
  VerifyAccountData,
  VerifyAccountResponse,
  ApiMessageResponse,
} from '../types/auth.ts';

export const authApi = {
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    return apiClient<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
      requiresAuth: false,
    });
  },

  register: async (data: RegisterData): Promise<AuthResponse> => {
    return apiClient<AuthResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
      requiresAuth: false,
    });
  },

  getMe: async (): Promise<User> => {
    return apiClient<User>('/auth/me', {
      method: 'GET',
      requiresAuth: true,
    });
  },

  forgotPassword: async (data: ForgotPasswordData): Promise<ApiMessageResponse> => {
    return apiClient<ApiMessageResponse>('/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify(data),
      requiresAuth: false,
    });
  },

  resetPassword: async (
    token: string,
    data: ResetPasswordData
  ): Promise<ApiMessageResponse> => {
    return apiClient<ApiMessageResponse>(`/auth/reset-password/${encodeURIComponent(token)}`, {
      method: 'POST',
      body: JSON.stringify(data),
      requiresAuth: false,
    });
  },

  verifyAccount: async (
    data: VerifyAccountData
  ): Promise<VerifyAccountResponse> => {
    return apiClient<VerifyAccountResponse>('/auth/verify', {
      method: 'POST',
      body: JSON.stringify(data),
      requiresAuth: false,
    });
  },
};
