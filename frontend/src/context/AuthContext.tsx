import {
  useState,
  useEffect,
  useCallback,
  type ReactNode,
} from 'react';
import type {
  User,
  LoginCredentials,
  RegisterData,
  ForgotPasswordData,
  ResetPasswordData,
  VerifyAccountData,
  AuthContextType,
} from '../types/auth.ts';
import {
  getStoredToken,
  setStoredToken,
  removeStoredToken,
} from '../api/client.ts';
import { authApi } from '../api/authApi.ts';
import { AuthContext } from './authContextInstance.ts';

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(getStoredToken());
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const logout = useCallback(() => {
    removeStoredToken();
    setToken(null);
    setUser(null);
  }, []);

  const refreshUser = useCallback(async () => {
    const currentToken = getStoredToken();
    if (!currentToken) {
      setUser(null);
      return;
    }

    try {
      const userData = await authApi.getMe();
      setUser(userData);
    } catch {
      // Falha ao obter usuário (token inválido ou expirado)
      logout();
    }
  }, [logout]);

  // Inicializa sessão a partir do token gravado
  useEffect(() => {
    const initializeAuth = async () => {
      const stored = getStoredToken();
      if (stored) {
        setToken(stored);
        try {
          const userData = await authApi.getMe();
          setUser(userData);
        } catch {
          // Token inválido/expirado
          removeStoredToken();
          setToken(null);
          setUser(null);
        }
      }
      setIsLoading(false);
    };

    initializeAuth();

    const handleUnauthorized = () => {
      logout();
    };

    window.addEventListener('arenaufcg:unauthorized', handleUnauthorized);
    return () => {
      window.removeEventListener('arenaufcg:unauthorized', handleUnauthorized);
    };
  }, [logout]);

  const login = async (credentials: LoginCredentials): Promise<void> => {
    const response = await authApi.login(credentials);
    setStoredToken(response.accessToken);
    setToken(response.accessToken);
    setUser(response.usuario);
  };

  const register = async (data: RegisterData): Promise<void> => {
    const response = await authApi.register(data);
    setStoredToken(response.accessToken);
    setToken(response.accessToken);
    setUser(response.usuario);
  };

  const forgotPassword = async (data: ForgotPasswordData): Promise<string> => {
    const response = await authApi.forgotPassword(data);
    return response.mensagem || 'E-mail de recuperação enviado com sucesso.';
  };

  const resetPassword = async (
    resetToken: string,
    data: ResetPasswordData
  ): Promise<string> => {
    const response = await authApi.resetPassword(resetToken, data);
    return response.mensagem || 'Senha redefinida com sucesso.';
  };

  const verifyAccount = async (data: VerifyAccountData): Promise<string> => {
    const response = await authApi.verifyAccount(data);
    if (user && user.email === data.email) {
      setUser({ ...user, verificado: true });
    }
    return response.mensagem || 'Conta verificada com sucesso.';
  };

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated: !!token && !!user,
    isLoading,
    login,
    register,
    logout,
    forgotPassword,
    resetPassword,
    verifyAccount,
    refreshUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
