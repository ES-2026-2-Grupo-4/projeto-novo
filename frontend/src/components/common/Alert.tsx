import type { ReactNode } from 'react';
import { AlertCircleIcon, CheckCircleIcon } from './Icons.tsx';

export type AlertVariant = 'error' | 'success' | 'warning' | 'info';

export interface AlertProps {
  variant?: AlertVariant;
  title?: string;
  children: ReactNode;
  onClose?: () => void;
  className?: string;
}

export function Alert({
  variant = 'error',
  title,
  children,
  onClose,
  className = '',
}: AlertProps) {
  const getIcon = () => {
    switch (variant) {
      case 'success':
        return <CheckCircleIcon className="alert-icon" />;
      case 'error':
      case 'warning':
      case 'info':
      default:
        return <AlertCircleIcon className="alert-icon" />;
    }
  };

  return (
    <div className={`alert-box alert-${variant} ${className}`} role="alert">
      <div className="alert-icon-wrapper">{getIcon()}</div>
      <div className="alert-body">
        {title && <strong className="alert-title">{title}</strong>}
        <div className="alert-content">{children}</div>
      </div>
      {onClose && (
        <button
          type="button"
          className="alert-close-btn"
          onClick={onClose}
          aria-label="Fechar mensagem"
        >
          &times;
        </button>
      )}
    </div>
  );
}
