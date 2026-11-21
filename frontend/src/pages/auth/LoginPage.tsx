import { useState, type FormEvent } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/useAuth.ts';
import { AuthLayout } from '../../components/layout/AuthLayout.tsx';
import { Input } from '../../components/common/Input.tsx';
import { Button } from '../../components/common/Button.tsx';
import { Alert } from '../../components/common/Alert.tsx';
import { MailIcon, LockIcon, ArrowRightIcon } from '../../components/common/Icons.tsx';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<{ email?: string; senha?: string }>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validate = (): boolean => {
    const errors: { email?: string; senha?: string } = {};

    if (!email.trim()) {
      errors.email = 'O e-mail é obrigatório.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      errors.email = 'Informe um endereço de e-mail válido.';
    }

    if (!senha) {
      errors.senha = 'A senha é obrigatória.';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!validate()) {
      return;
    }

    setIsSubmitting(true);

    try {
      await login({
        email: email.trim(),
        senha,
      });

      // Redireciona para a rota anterior ou dashboard
      const state = location.state as { from?: { pathname: string } } | null;
      const targetPath = state?.from?.pathname || '/dashboard';
      navigate(targetPath, { replace: true });
    } catch (err: unknown) {
      const message =
        err instanceof Error
          ? err.message
          : 'Falha na autenticação. Verifique suas credenciais.';
      setErrorMessage(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title="Acesse sua conta"
      subtitle="Entre com suas credenciais para acessar o painel do campeonato"
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

      <form onSubmit={handleSubmit} noValidate className="auth-form">
        <Input
          label="E-mail"
          type="email"
          name="email"
          value={email}
          onChange={(e) => {
            setEmail(e.target.value);
            if (fieldErrors.email) setFieldErrors({ ...fieldErrors, email: undefined });
          }}
          placeholder="seu.email@exemplo.com"
          leftIcon={<MailIcon />}
          error={fieldErrors.email}
          required
          autoComplete="email"
          autoFocus
        />

        <div className="password-field-container">
          <Input
            label="Senha"
            type="password"
            name="senha"
            value={senha}
            onChange={(e) => {
              setSenha(e.target.value);
              if (fieldErrors.senha) setFieldErrors({ ...fieldErrors, senha: undefined });
            }}
            placeholder="Digite sua senha"
            leftIcon={<LockIcon />}
            showPasswordToggle
            error={fieldErrors.senha}
            required
            autoComplete="current-password"
          />
          <div className="forgot-password-link-wrapper">
            <Link to="/esqueci-senha" className="auth-text-link">
              Esqueceu sua senha?
            </Link>
          </div>
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
          {isSubmitting ? 'Entrando...' : 'Entrar na plataforma'}
        </Button>
      </form>

      <div className="auth-card-footer">
        <p className="auth-footer-prompt">
          Ainda não possui uma conta?{' '}
          <Link to="/cadastro" className="auth-text-link-primary">
            Cadastre-se agora
          </Link>
        </p>
        <p className="auth-footer-prompt auth-verify-prompt">
          Precisa validar seu e-mail?{' '}
          <Link to="/verificar-conta" className="auth-text-link">
            Validar código
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
}
