import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/useAuth.ts';
import { AuthLayout } from '../../components/layout/AuthLayout.tsx';
import { Input } from '../../components/common/Input.tsx';
import { Button } from '../../components/common/Button.tsx';
import { Alert } from '../../components/common/Alert.tsx';
import { MailIcon, ArrowLeftIcon, ArrowRightIcon, KeyRoundIcon } from '../../components/common/Icons.tsx';

export function ForgotPasswordPage() {
  const { forgotPassword } = useAuth();

  const [email, setEmail] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [fieldError, setFieldError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validate = (): boolean => {
    if (!email.trim()) {
      setFieldError('O e-mail é obrigatório.');
      return false;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      setFieldError('Informe um e-mail válido.');
      return false;
    }
    setFieldError(null);
    return true;
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
      const responseMsg = await forgotPassword({ email: email.trim() });
      setSuccessMessage(
        responseMsg ||
          'Se o e-mail estiver cadastrado, enviaremos as instruções com o token de recuperação.'
      );
      setEmail('');
    } catch (err: unknown) {
      const message =
        err instanceof Error
          ? err.message
          : 'Não foi possível solicitar a redefinição de senha.';
      setErrorMessage(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title="Recuperar senha"
      subtitle="Informe seu e-mail para receber as instruções e o token de redefinição"
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

      {successMessage && (
        <div className="mb-4">
          <Alert variant="success">{successMessage}</Alert>
          <div className="reset-token-helper-box mt-3">
            <p className="reset-helper-text">
              Já recebeu o token de recuperação por e-mail?
            </p>
            <Link
              to="/redefinir-senha"
              className="btn btn-outline btn-sm full-width-sm"
            >
              <KeyRoundIcon className="btn-icon" /> Inserir token e definir nova senha
            </Link>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate className="auth-form">
        <Input
          label="E-mail cadastrado"
          type="email"
          name="email"
          value={email}
          onChange={(e) => {
            setEmail(e.target.value);
            if (fieldError) setFieldError(null);
          }}
          placeholder="exemplo@computacao.ufcg.edu.br"
          leftIcon={<MailIcon />}
          error={fieldError || undefined}
          required
          autoComplete="email"
          autoFocus
        />

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
          {isSubmitting ? 'Enviando solicitação...' : 'Enviar instruções'}
        </Button>
      </form>

      <div className="auth-card-footer">
        <Link to="/login" className="auth-back-link">
          <ArrowLeftIcon /> Voltar para o login
        </Link>
      </div>
    </AuthLayout>
  );
}
