export type UserRole = 'ORGANIZADOR' | 'DIRIGENTE' | 'ARBITRO' | 'ESPECTADOR';

export interface User {
  id: string;
  nome: string;
  email: string;
  perfil: UserRole;
  verificado: boolean;
  criadoEm?: string;
}

export interface AuthResponse {
  accessToken: string;
  usuario: User;
}

export interface LoginCredentials {
  email: string;
  senha: string;
}

export interface RegisterData {
  nome: string;
  email: string;
  senha: string;
  confirmaSenha: string;
  perfil: UserRole;
  aceiteTermos: boolean;
}

export interface ForgotPasswordData {
  email: string;
}

export interface ResetPasswordData {
  senha: string;
  confirmaSenha: string;
}

export interface VerifyAccountData {
  email: string;
  codigoVerificacao: string;
}

export interface VerifyAccountResponse {
  mensagem: string;
  verificado: boolean;
}

export interface ApiMessageResponse {
  mensagem: string;
}

export interface ApiErrorResponse {
  codigo?: string;
  mensagem?: string;
  message?: string;
  errors?: Record<string, string> | string[];
}

export interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginCredentials) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
  forgotPassword: (data: ForgotPasswordData) => Promise<string>;
  resetPassword: (token: string, data: ResetPasswordData) => Promise<string>;
  verifyAccount: (data: VerifyAccountData) => Promise<string>;
  refreshUser: () => Promise<void>;
}
