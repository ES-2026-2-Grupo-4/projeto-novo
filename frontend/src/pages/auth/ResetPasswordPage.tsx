import { useState, type FormEvent } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/useAuth.ts';
import { AuthLayout } from '../../components/layout/AuthLayout.tsx';
import { Input } from '../../components/common/Input.tsx';
import { Button } from '../../components/common/Button.tsx';
import { Alert } from '../../components/common/Alert.tsx';
import {
  LockIcon,
  KeyRoundIcon,
  CheckCircleIcon,
  ArrowRightIcon,
  ArrowLeftIcon,
} from '../../components/common/Icons.tsx';

export function ResetPasswordPage() {
  const { token: urlToken } = useParams<{ token?: string }>();
  const { resetPassword } = useAuth();
  const navigate = useNavigate();

  const [inputToken, setInputToken] = useState(urlToken || '');
  const [senha, setSenha] = useState('');
  const [confirmaSenha, setConfirmaSenha] = useState('');

  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const activeToken = urlToken || inputToken;

  // Validações em tempo real
  const hasMinLength = senha.length >= 8 && senha.length <= 72;
  const hasLetterAndNumber = /^(?=.*[A-Za-z])(?=.*\d).+$/.test(senha);
  const passwordsMatch = senha.length > 0 && senha === confirmaSenha;

  const validate = (): boolean => {
    const errors: Record<string, string> = {};

    if (!activeToken.trim()) {
      errors.token = 'O token de recuperação é obrigatório.';
    }

    if (!senha) {
      errors.senha = 'A nova senha é obrigatória.';
    } else if (senha.length < 8 || senha.length > 72) {
      errors.senha = 'A senha deve conter entre 8 e 72 caracteres.';
    } else if (!/^(?=.*[A-Za-z])(?=.*\d).+$/.test(senha)) {
      errors.senha = 'A senha deve conter pelo menos uma letra e um número.';
    }

    if (!confirmaSenha) {
      errors.confirmaSenha = 'A confirmação de senha é obrigatória.';
    } else if (senha !== confirmaSenha) {
      errors.confirmaSenha = 'As senhas não coincidem.';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);

    if (!validate()) {
      return;
    }

    setIsSubmitting(true);

    try {
      const responseMsg = await resetPassword(activeToken.trim(), {
        senha,
        confirmaSenha,
      });

      setSuccessMessage(
        responseMsg || 'Senha redefinida com sucesso! Você já pode realizar o login.'
      );

      // Limpa os campos
      setSenha('');
      setConfirmaSenha('');

      // Redireciona para o login após 3 segundos
      setTimeout(() => {
        navigate('/login');
      }, 3000);
    } catch (err: unknown) {
      const message =
        err instanceof Error
          ? err.message
          : 'Não foi possível redefinir a senha. O token pode ser inválido ou ter expirado.';
      setErrorMessage(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title="Redefinição de senha"
      subtitle="Defina uma nova senha segura para sua conta"
    >
      {errorMessage && (
        <Alert
          variant="error"
          onClose={() => setErrorMessage(null)}
          className="mb-4"
        >
          {errorMessage}
        </Alert>
      )}

      {successMessage ? (
        <div className="reset-success-container">
          <Alert variant="success">{successMessage}</Alert>
          <p className="redirect-note">
            Redirecionando para a página de login em instantes...
          </p>
          <Button
            type="button"
            variant="primary"
            fullWidth
            onClick={() => navigate('/login')}
            className="mt-3"
          >
            Ir para o Login agora
          </Button>
        </div>
      ) : (
        <form onSubmit={handleSubmit} noValidate className="auth-form">
          <Input
            label="Token de recuperação"
            type="text"
            name="token"
            value={activeToken}
            onChange={(e) => {
              setInputToken(e.target.value);
              if (fieldErrors.token) setFieldErrors({ ...fieldErrors, token: '' });
            }}
            placeholder="Cole aqui o token recebido por e-mail"
            leftIcon={<KeyRoundIcon />}
            error={fieldErrors.token}
            required
            helperText="O token enviado para seu e-mail para autorizar a troca de senha."
          />

          <Input
            label="Nova senha"
            type="password"
            name="senha"
            value={senha}
            onChange={(e) => {
              setSenha(e.target.value);
              if (fieldErrors.senha) setFieldErrors({ ...fieldErrors, senha: '' });
            }}
            placeholder="Nova senha (8 a 72 caracteres)"
            leftIcon={<LockIcon />}
            showPasswordToggle
            error={fieldErrors.senha}
            required
            autoComplete="new-password"
          />

          <Input
            label="Confirmar nova senha"
            type="password"
            name="confirmaSenha"
            value={confirmaSenha}
            onChange={(e) => {
              setConfirmaSenha(e.target.value);
              if (fieldErrors.confirmaSenha)
                setFieldErrors({ ...fieldErrors, confirmaSenha: '' });
            }}
            placeholder="Repita a nova senha"
            leftIcon={<LockIcon />}
            showPasswordToggle
            error={fieldErrors.confirmaSenha}
            required
            autoComplete="new-password"
          />

          <div className="password-rules-box">
            <span className="password-rules-title">Requisitos da senha:</span>
            <ul className="password-rules-list">
              <li className={hasMinLength ? 'rule-met' : 'rule-unmet'}>
                <CheckCircleIcon className="rule-icon" /> 8 a 72 caracteres
              </li>
              <li className={hasLetterAndNumber ? 'rule-met' : 'rule-unmet'}>
                <CheckCircleIcon className="rule-icon" /> Pelo menos uma letra e um número
              </li>
              <li className={passwordsMatch ? 'rule-met' : 'rule-unmet'}>
                <CheckCircleIcon className="rule-icon" /> Senhas conferem
              </li>
            </ul>
          </div>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            fullWidth
            isLoading={isSubmitting}
            icon={<ArrowRightIcon />}
            iconPosition="right"
            className="mt-2"
          >
            {isSubmitting ? 'Redefinindo senha...' : 'Salvar nova senha'}
          </Button>
        </form>
      )}

      <div className="auth-card-footer">
        <Link to="/login" className="auth-back-link">
          <ArrowLeftIcon /> Voltar para o login
        </Link>
      </div>
    </AuthLayout>
  );
}
