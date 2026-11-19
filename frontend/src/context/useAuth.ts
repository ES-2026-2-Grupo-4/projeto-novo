import { useContext } from 'react';
import { AuthContext } from './authContextInstance.ts';
import type { AuthContextType } from '../types/auth.ts';

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth deve ser utilizado dentro de um AuthProvider');
  }
  return context;
}
