import { useState, useId, forwardRef, type InputHTMLAttributes, type ReactNode } from 'react';
import { EyeIcon, EyeOffIcon } from './Icons.tsx';

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
  leftIcon?: ReactNode;
  rightIcon?: ReactNode;
  showPasswordToggle?: boolean;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  (
    {
      label,
      error,
      helperText,
      leftIcon,
      rightIcon,
      showPasswordToggle = false,
      type = 'text',
      className = '',
      id,
      required,
      ...props
    },
    ref
  ) => {
    const generatedId = useId();
    const inputId = id || generatedId;
    const [isPasswordVisible, setIsPasswordVisible] = useState(false);

    const isPassword = type === 'password';
    const computedType = isPassword ? (isPasswordVisible ? 'text' : 'password') : type;

    return (
      <div className={`input-group ${error ? 'has-error' : ''} ${className}`}>
        {label && (
          <label htmlFor={inputId} className="input-label">
            {label}
            {required && <span className="input-required-mark">*</span>}
          </label>
        )}

        <div className="input-wrapper">
          {leftIcon && <span className="input-icon-left">{leftIcon}</span>}

          <input
            ref={ref}
            id={inputId}
            type={computedType}
            className={`input-field ${leftIcon ? 'with-left-icon' : ''} ${
              rightIcon || (isPassword && showPasswordToggle) ? 'with-right-icon' : ''
            }`}
            aria-invalid={!!error}
            aria-describedby={
              error ? `${inputId}-error` : helperText ? `${inputId}-helper` : undefined
            }
            required={required}
            {...props}
          />

          {isPassword && showPasswordToggle ? (
            <button
              type="button"
              className="input-password-toggle"
              onClick={() => setIsPasswordVisible((prev) => !prev)}
              aria-label={isPasswordVisible ? 'Ocultar senha' : 'Exibir senha'}
              tabIndex={-1}
            >
              {isPasswordVisible ? <EyeOffIcon /> : <EyeIcon />}
            </button>
          ) : rightIcon ? (
            <span className="input-icon-right">{rightIcon}</span>
          ) : null}
        </div>

        {error && (
          <p id={`${inputId}-error`} className="input-error-msg" role="alert">
            {error}
          </p>
        )}

        {!error && helperText && (
          <p id={`${inputId}-helper`} className="input-helper-msg">
            {helperText}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';
