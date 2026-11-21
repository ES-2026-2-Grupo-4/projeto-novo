import { useId, forwardRef, type SelectHTMLAttributes, type ReactNode } from 'react';

export interface SelectOption {
  value: string;
  label: string;
  description?: string;
}

export interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  options: SelectOption[];
  error?: string;
  helperText?: string;
  leftIcon?: ReactNode;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
  (
    {
      label,
      options,
      error,
      helperText,
      leftIcon,
      className = '',
      id,
      required,
      ...props
    },
    ref
  ) => {
    const generatedId = useId();
    const selectId = id || generatedId;

    return (
      <div className={`input-group ${error ? 'has-error' : ''} ${className}`}>
        {label && (
          <label htmlFor={selectId} className="input-label">
            {label}
            {required && <span className="input-required-mark">*</span>}
          </label>
        )}

        <div className="input-wrapper select-wrapper">
          {leftIcon && <span className="input-icon-left">{leftIcon}</span>}

          <select
            ref={ref}
            id={selectId}
            className={`input-field select-field ${leftIcon ? 'with-left-icon' : ''}`}
            aria-invalid={!!error}
            required={required}
            {...props}
          >
            {options.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>

          <span className="select-arrow" aria-hidden="true">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="m6 9 6 6 6-6" />
            </svg>
          </span>
        </div>

        {error && (
          <p id={`${selectId}-error`} className="input-error-msg" role="alert">
            {error}
          </p>
        )}

        {!error && helperText && (
          <p id={`${selectId}-helper`} className="input-helper-msg">
            {helperText}
          </p>
        )}
      </div>
    );
  }
);

Select.displayName = 'Select';
