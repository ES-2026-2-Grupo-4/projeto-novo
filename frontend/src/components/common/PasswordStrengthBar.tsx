interface PasswordStrengthBarProps {
  password: string;
}

interface StrengthResult {
  score: number; // 0 a 4
  label: string;
  colorClass: string;
}

function calculatePasswordStrength(password: string): StrengthResult {
  if (!password) {
    return { score: 0, label: 'Não informada', colorClass: 'strength-none' };
  }

  let score = 0;

  // Critério 1: Tamanho mínimo
  if (password.length >= 8) score++;
  if (password.length >= 12) score++;

  // Critério 2: Letras maiúsculas e minúsculas
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++;

  // Critério 3: Números
  if (/\d/.test(password)) score++;

  // Critério 4: Caracteres especiais
  if (/[^A-Za-z0-9]/.test(password)) score++;

  // Normaliza pontuação de 1 a 4
  if (password.length < 8) {
    return { score: 1, label: 'Muito fraca (mínimo 8 caracteres)', colorClass: 'strength-very-weak' };
  }

  if (score <= 2) {
    return { score: 1, label: 'Muito fraca', colorClass: 'strength-very-weak' };
  } else if (score === 3) {
    return { score: 2, label: 'Fraca / Razoável', colorClass: 'strength-weak' };
  } else if (score === 4) {
    return { score: 3, label: 'Boa', colorClass: 'strength-medium' };
  } else {
    return { score: 4, label: 'Forte e Segura', colorClass: 'strength-strong' };
  }
}

export function PasswordStrengthBar({ password }: PasswordStrengthBarProps) {
  const { score, label, colorClass } = calculatePasswordStrength(password);

  return (
    <div className="password-strength-container" aria-live="polite">
      <div className="strength-header">
        <span className="strength-title">Força da senha:</span>
        <span className={`strength-label ${colorClass}`}>{label}</span>
      </div>

      <div className="strength-bars-track">
        {[1, 2, 3, 4].map((step) => (
          <div
            key={step}
            className={`strength-segment ${step <= score ? colorClass : 'segment-empty'}`}
          />
        ))}
      </div>
    </div>
  );
}
