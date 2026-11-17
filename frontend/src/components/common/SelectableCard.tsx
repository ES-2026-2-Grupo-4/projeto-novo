import type { ReactNode, KeyboardEvent } from 'react';
import { CheckCircleIcon } from './Icons.tsx';

export interface SelectableCardProps {
  icon: ReactNode;
  title: string;
  description?: string;
  badge?: string;
  selected: boolean;
  onClick: () => void;
  multiSelect?: boolean;
  className?: string;
  disabled?: boolean;
}

export function SelectableCard({
  icon,
  title,
  description,
  badge,
  selected,
  onClick,
  multiSelect = false,
  className = '',
  disabled = false,
}: SelectableCardProps) {
  const handleKeyDown = (e: KeyboardEvent<HTMLDivElement>) => {
    if (disabled) return;
    if (e.key === ' ' || e.key === 'Enter') {
      e.preventDefault();
      onClick();
    }
  };

  return (
    <div
      role={multiSelect ? 'checkbox' : 'radio'}
      aria-checked={selected}
      tabIndex={disabled ? -1 : 0}
      onClick={disabled ? undefined : onClick}
      onKeyDown={handleKeyDown}
      className={`selectable-card ${selected ? 'is-selected' : ''} ${
        disabled ? 'is-disabled' : ''
      } ${className}`}
    >
      <div className="card-top-row">
        <div className={`card-icon-bubble ${selected ? 'icon-bubble-selected' : ''}`}>
          {icon}
        </div>

        <div className="card-indicators">
          {badge && <span className="card-badge">{badge}</span>}
          <div className={`card-check-badge ${selected ? 'checked' : 'unchecked'}`}>
            <CheckCircleIcon />
          </div>
        </div>
      </div>

      <div className="card-body">
        <h4 className="card-item-title">{title}</h4>
        {description && <p className="card-item-description">{description}</p>}
      </div>
    </div>
  );
}
