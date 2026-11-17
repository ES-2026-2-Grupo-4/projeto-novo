import { useId, forwardRef, type InputHTMLAttributes, type ReactNode } from 'react';

export interface CheckboxProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label?: ReactNode;
  error?: string;
}

export const Checkbox = forwardRef<HTMLInputElement, CheckboxProps>(
  ({ label, error, className = '', id, children, ...props }, ref) => {
    const generatedId = useId();
    const checkboxId = id || generatedId;

    return (
      <div className={`checkbox-container ${error ? 'has-error' : ''} ${className}`}>
        <label htmlFor={checkboxId} className="checkbox-label">
          <input
            ref={ref}
            id={checkboxId}
            type="checkbox"
            className="checkbox-input"
            aria-invalid={!!error}
            {...props}
          />
          <span className="checkbox-custom" aria-hidden="true">
            <svg
              className="checkbox-check-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="3"
            >
              <polyline points="20 6 9 17 4 12" />
            </svg>
          </span>
          <span className="checkbox-text">{label || children}</span>
        </label>
        {error && (
          <p className="input-error-msg" role="alert">
            {error}
          </p>
        )}
      </div>
    );
  }
);

Checkbox.displayName = 'Checkbox';
