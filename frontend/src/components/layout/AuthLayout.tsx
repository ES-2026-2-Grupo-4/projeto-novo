import type { ReactNode } from 'react';
import { TrophyIcon } from '../common/Icons.tsx';

interface AuthLayoutProps {
  title: string;
  subtitle?: string;
  children: ReactNode;
}

export function AuthLayout({ title, subtitle, children }: AuthLayoutProps) {
  return (
    <div className="auth-page-container">
      <div className="auth-brand-header">
        <div className="auth-logo-badge">
          <TrophyIcon className="auth-logo-icon" />
        </div>
        <h1 className="auth-brand-name">Arena UFCG</h1>
        <p className="auth-brand-tagline">Sistema de Gestão de Campeonatos Esportivos</p>
      </div>

      <div className="auth-card-wrapper">
        <div className="auth-card">
          <div className="auth-card-header">
            <h2 className="auth-card-title">{title}</h2>
            {subtitle && <p className="auth-card-subtitle">{subtitle}</p>}
          </div>

          <div className="auth-card-content">{children}</div>
        </div>
      </div>

      <footer className="auth-footer">
        <p>&copy; {new Date().getFullYear()} Arena UFCG &bull; Universidade Federal de Campina Grande</p>
      </footer>
    </div>
  );
}
