import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/useAuth.ts';
import { AuthLayout } from '../../components/layout/AuthLayout.tsx';
import { Input } from '../../components/common/Input.tsx';
import { Button } from '../../components/common/Button.tsx';
import { Alert } from '../../components/common/Alert.tsx';
import {
  MailIcon,
  ShieldIcon,
  CheckCircleIcon,
  ArrowLeftIcon,
} from '../../components/common/Icons.tsx';

export function VerifyAccountPage() {
  const { verifyAccount, user } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState(user?.email || '');
  const [codigoVerificacao, setCodigoVerificacao] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validate = (): boolean => {
    const errors: Record<string, string> = {};

    if (!email.trim()) {
      errors.email = 'O e-mail é obrigatório.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      errors.email = 'Informe um e-mail válido.';
    }

    if (!codigoVerificacao.trim()) {
      errors.codigo = 'O código de verificação é obrigatório.';
    } else if (!/^\d{6}$/.test(codigoVerificacao.trim())) {
      errors.codigo = 'O código deve conter exatamente 6 dígitos numéricos.';
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
      const responseMsg = await verifyAccount({
        email: email.trim(),
        codigoVerificacao: codigoVerificacao.trim(),
      });

      setSuccessMessage(responseMsg || 'Conta verificada com sucesso!');

      setTimeout(() => {
        if (user) {
          navigate('/dashboard');
        } else {
          navigate('/login');
        }
      }, 2500);
    } catch (err: unknown) {
      const message =
        err instanceof Error
          ? err.message
          : 'Falha ao validar conta. Verifique o código e tente novamente.';
      setErrorMessage(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title="Verificação de conta"
      subtitle="Digite o código numérico de 6 dígitos enviado para seu e-mail"
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
        <div>
          <Alert variant="success">{successMessage}</Alert>
          <Button
            type="button"
            variant="primary"
            fullWidth
            onClick={() => (user ? navigate('/dashboard') : navigate('/login'))}
            className="mt-4"
          >
            {user ? 'Voltar para o Dashboard' : 'Ir para o Login'}
          </Button>
        </div>
      ) : (
        <form onSubmit={handleSubmit} noValidate className="auth-form">
          <Input
            label="E-mail da conta"
            type="email"
            name="email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              if (fieldErrors.email) setFieldErrors({ ...fieldErrors, email: '' });
            }}
            placeholder="seu.email@exemplo.com"
            leftIcon={<MailIcon />}
            error={fieldErrors.email}
            required
            autoComplete="email"
          />

          <Input
            label="Código de verificação (6 dígitos)"
            type="text"
            name="codigoVerificacao"
            maxLength={6}
            value={codigoVerificacao}
            onChange={(e) => {
              const onlyNums = e.target.value.replace(/\D/g, '').slice(0, 6);
              setCodigoVerificacao(onlyNums);
              if (fieldErrors.codigo) setFieldErrors({ ...fieldErrors, codigo: '' });
            }}
            placeholder="000000"
            leftIcon={<ShieldIcon />}
            error={fieldErrors.codigo}
            helperText="Insira o código numérico de 6 dígitos recebido por e-mail."
            required
            autoFocus
          />

          <Button
            type="submit"
            variant="primary"
            size="lg"
            fullWidth
            isLoading={isSubmitting}
            icon={<CheckCircleIcon />}
            className="mt-2"
          >
            {isSubmitting ? 'Verificando...' : 'Confirmar código'}
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
