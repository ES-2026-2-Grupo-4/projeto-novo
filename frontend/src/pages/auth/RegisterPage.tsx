import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/useAuth.ts';
import { AuthLayout } from '../../components/layout/AuthLayout.tsx';
import { Input } from '../../components/common/Input.tsx';
import { Select, type SelectOption } from '../../components/common/Select.tsx';
import { Checkbox } from '../../components/common/Checkbox.tsx';
import { Button } from '../../components/common/Button.tsx';
import { Alert } from '../../components/common/Alert.tsx';
import {
  UserIcon,
  MailIcon,
  LockIcon,
  ShieldIcon,
  ArrowRightIcon,
  CheckCircleIcon,
} from '../../components/common/Icons.tsx';
import type { UserRole } from '../../types/auth.ts';

const ROLE_OPTIONS: SelectOption[] = [
  { value: 'ORGANIZADOR', label: 'Organizador - Criação e gestão de campeonatos' },
  { value: 'DIRIGENTE', label: 'Dirigente - Inscrição e gestão de equipes' },
  { value: 'ARBITRO', label: 'Árbitro - Súmulas e validação de partidas' },
  { value: 'ESPECTADOR', label: 'Espectador - Acompanhamento dos jogos' },
];

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [perfil, setPerfil] = useState<UserRole>('ESPECTADOR');
  const [senha, setSenha] = useState('');
  const [confirmaSenha, setConfirmaSenha] = useState('');
  const [aceiteTermos, setAceiteTermos] = useState(false);

  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Validações em tempo real da senha
  const hasMinLength = senha.length >= 8 && senha.length <= 72;
  const hasLetterAndNumber = /^(?=.*[A-Za-z])(?=.*\d).+$/.test(senha);
  const passwordsMatch = senha.length > 0 && senha === confirmaSenha;

  const validate = (): boolean => {
    const errors: Record<string, string> = {};

    if (!nome.trim()) {
      errors.nome = 'O nome completo é obrigatório.';
    } else if (nome.trim().length > 120) {
      errors.nome = 'O nome deve ter no máximo 120 caracteres.';
    }

    if (!email.trim()) {
      errors.email = 'O e-mail é obrigatório.';
    } else if (
      !/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email.trim())
    ) {
      errors.email = 'Informe um endereço de e-mail válido.';
    } else if (email.trim().length > 180) {
      errors.email = 'O e-mail deve ter no máximo 180 caracteres.';
    }

    if (!senha) {
      errors.senha = 'A senha é obrigatória.';
    } else if (senha.length < 8 || senha.length > 72) {
      errors.senha = 'A senha deve conter entre 8 e 72 caracteres.';
    } else if (!/^(?=.*[A-Za-z])(?=.*\d).+$/.test(senha)) {
      errors.senha = 'A senha deve conter pelo menos uma letra e um número.';
    }

    if (!confirmaSenha) {
      errors.confirmaSenha = 'Confirme sua senha.';
    } else if (senha !== confirmaSenha) {
      errors.confirmaSenha = 'As senhas não coincidem.';
    }

    if (!aceiteTermos) {
      errors.aceiteTermos =
        'Você deve aceitar os Termos de Uso e a Política de Privacidade.';
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
      await register({
        nome: nome.trim(),
        email: email.trim(),
        senha,
        confirmaSenha,
        perfil,
        aceiteTermos,
      });

      navigate('/dashboard', { replace: true });
    } catch (err: unknown) {
      const message =
        err instanceof Error
          ? err.message
          : 'Não foi possível concluir o cadastro. Verifique os dados informados.';
      setErrorMessage(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title="Crie sua conta"
      subtitle="Cadastre-se para participar das competições esportivas da UFCG"
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
          label="Nome completo"
          type="text"
          name="nome"
          value={nome}
          onChange={(e) => {
            setNome(e.target.value);
            if (fieldErrors.nome) setFieldErrors({ ...fieldErrors, nome: '' });
          }}
          placeholder="Ex: João Silva"
          leftIcon={<UserIcon />}
          error={fieldErrors.nome}
          required
          autoComplete="name"
        />

        <Input
          label="E-mail institucional ou pessoal"
          type="email"
          name="email"
          value={email}
          onChange={(e) => {
            setEmail(e.target.value);
            if (fieldErrors.email) setFieldErrors({ ...fieldErrors, email: '' });
          }}
          placeholder="exemplo@computacao.ufcg.edu.br"
          leftIcon={<MailIcon />}
          error={fieldErrors.email}
          required
          autoComplete="email"
        />

        <Select
          label="Perfil de acesso na Arena"
          options={ROLE_OPTIONS}
          value={perfil}
          onChange={(e) => setPerfil(e.target.value as UserRole)}
          leftIcon={<ShieldIcon />}
          helperText="Escolha a função principal que você desempenha nas competições."
          required
        />

        <Input
          label="Senha de acesso"
          type="password"
          name="senha"
          value={senha}
          onChange={(e) => {
            setSenha(e.target.value);
            if (fieldErrors.senha) setFieldErrors({ ...fieldErrors, senha: '' });
          }}
          placeholder="Mínimo de 8 caracteres com letras e números"
          leftIcon={<LockIcon />}
          showPasswordToggle
          error={fieldErrors.senha}
          required
          autoComplete="new-password"
        />

        <Input
          label="Confirmação de senha"
          type="password"
          name="confirmaSenha"
          value={confirmaSenha}
          onChange={(e) => {
            setConfirmaSenha(e.target.value);
            if (fieldErrors.confirmaSenha)
              setFieldErrors({ ...fieldErrors, confirmaSenha: '' });
          }}
          placeholder="Repita sua senha"
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

        <Checkbox
          name="aceiteTermos"
          checked={aceiteTermos}
          onChange={(e) => {
            setAceiteTermos(e.target.checked);
            if (fieldErrors.aceiteTermos)
              setFieldErrors({ ...fieldErrors, aceiteTermos: '' });
          }}
          error={fieldErrors.aceiteTermos}
          required
          label={
            <span>
              Declaro que li e aceito os{' '}
              <a
                href="#termos"
                onClick={(e) => {
                  e.preventDefault();
                  alert('Termos de Uso e Política de Privacidade da Arena UFCG.');
                }}
                className="auth-text-link"
              >
                Termos de Uso
              </a>{' '}
              e a Política de Privacidade.
            </span>
          }
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
          {isSubmitting ? 'Cadastrando...' : 'Finalizar cadastro'}
        </Button>
      </form>

      <div className="auth-card-footer">
        <p className="auth-footer-prompt">
          Já possui uma conta cadastrada?{' '}
          <Link to="/login" className="auth-text-link-primary">
            Fazer login
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
}
