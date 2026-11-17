import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/useAuth.ts';
import { AuthLayout } from '../../components/layout/AuthLayout.tsx';
import { Input } from '../../components/common/Input.tsx';
import { Checkbox } from '../../components/common/Checkbox.tsx';
import { Button } from '../../components/common/Button.tsx';
import { Alert } from '../../components/common/Alert.tsx';
import { PasswordStrengthBar } from '../../components/common/PasswordStrengthBar.tsx';
import { SelectableCard } from '../../components/common/SelectableCard.tsx';
import { StepProgress, type StepItem } from '../../components/common/StepProgress.tsx';
import {
  UserIcon,
  MailIcon,
  LockIcon,
  PhoneIcon,
  MapPinIcon,
  ArrowRightIcon,
  ArrowLeftIcon,
  CheckCircleIcon,
  CalendarCheckIcon,
  UsersIcon,
  WhistleIcon,
  MegaphoneIcon,
  SoccerBallIcon,
  BasketballIcon,
  VolleyballIcon,
  GamepadIcon,
  MedalIcon,
  FlameIcon,
  SparklesIcon,
  TrophyIcon,
} from '../../components/common/Icons.tsx';
import type { UserRole, NivelCompeticao } from '../../types/auth.ts';

const STEPS: StepItem[] = [
  { number: 1, title: 'Dados Pessoais & Acesso', shortTitle: 'Dados' },
  { number: 2, title: 'Perfil na Arena', shortTitle: 'Perfil' },
  { number: 3, title: 'Modalidades & Nível', shortTitle: 'Esportes' },
];

interface RoleCardOption {
  role: UserRole;
  title: string;
  description: string;
  icon: React.ReactNode;
}

const ROLE_OPTIONS: RoleCardOption[] = [
  {
    role: 'ORGANIZADOR',
    title: 'Organizador',
    description: 'Criação e gestão de campeonatos, regulamentos, tabelas de jogos e aprovação de equipes.',
    icon: <CalendarCheckIcon />,
  },
  {
    role: 'DIRIGENTE',
    title: 'Dirigente Esportivo',
    description: 'Representação de times e atléticas, inscrição de equipes e gerenciamento de atletas.',
    icon: <UsersIcon />,
  },
  {
    role: 'ARBITRO',
    title: 'Árbitro / Oficial',
    description: 'Atuação em campo/quadra, apontamento de súmulas, cartões e confirmação de resultados.',
    icon: <WhistleIcon />,
  },
  {
    role: 'ESPECTADOR',
    title: 'Espectador',
    description: 'Acompanhamento dos jogos, tabelas de classificação, estatísticas e calendário esportivo.',
    icon: <MegaphoneIcon />,
  },
];

interface ModalidadeOption {
  id: string;
  title: string;
  description: string;
  icon: React.ReactNode;
}

const MODALIDADE_OPTIONS: ModalidadeOption[] = [
  {
    id: 'FUTEBOL',
    title: 'Futebol',
    description: 'Futebol de campo tradicional',
    icon: <SoccerBallIcon />,
  },
  {
    id: 'FUTSAL',
    title: 'Futsal',
    description: 'Futsal indoor universitário',
    icon: <SoccerBallIcon />,
  },
  {
    id: 'BASQUETE',
    title: 'Basquete',
    description: 'Basquetebol 5x5 e 3x3',
    icon: <BasketballIcon />,
  },
  {
    id: 'VOLEI',
    title: 'Voleibol',
    description: 'Vôlei de quadra e areia',
    icon: <VolleyballIcon />,
  },
  {
    id: 'HANDEBOL',
    title: 'Handebol',
    description: 'Handebol de quadra',
    icon: <TrophyIcon />,
  },
  {
    id: 'E_SPORTS',
    title: 'E-Sports',
    description: 'Fifa, LoL, CS e Valorant',
    icon: <GamepadIcon />,
  },
];

interface NivelOption {
  nivel: NivelCompeticao;
  title: string;
  description: string;
  icon: React.ReactNode;
  badge?: string;
}

const NIVEL_OPTIONS: NivelOption[] = [
  {
    nivel: 'RECREATIVO',
    title: 'Recreativo',
    description: 'Prática por diversão, integração, saúde e lazer no campus.',
    icon: <SparklesIcon />,
    badge: 'Iniciante',
  },
  {
    nivel: 'AMADOR',
    title: 'Amador',
    description: 'Disputas regulares em campeonatos universitários e torneios internos.',
    icon: <MedalIcon />,
    badge: 'Intermediário',
  },
  {
    nivel: 'SEMIPRO',
    title: 'Semiprofissional',
    description: 'Alto rendimento, treinos táticos e disputa pelos principais títulos da UFCG.',
    icon: <FlameIcon />,
    badge: 'Competitivo',
  },
];

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  // Controle de Etapa (1, 2 ou 3)
  const [currentStep, setCurrentStep] = useState(1);

  // Etapa 1: Dados Pessoais & Acesso
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [telefone, setTelefone] = useState('');
  const [local, setLocal] = useState('Campina Grande/PB');
  const [senha, setSenha] = useState('');
  const [confirmaSenha, setConfirmaSenha] = useState('');

  // Etapa 2: Perfil
  const [perfil, setPerfil] = useState<UserRole>('ORGANIZADOR');

  // Etapa 3: Modalidades & Nível & Termos
  const [modalidades, setModalidades] = useState<string[]>(['FUTEBOL', 'FUTSAL']);
  const [nivelCompeticao, setNivelCompeticao] = useState<NivelCompeticao>('AMADOR');
  const [aceiteTermos, setAceiteTermos] = useState(false);

  // Estados de Validação e Envio
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Formatação de Telefone: (83) 90000-0000
  const handlePhoneChange = (val: string) => {
    const raw = val.replace(/\D/g, '').slice(0, 11);
    let formatted = raw;
    if (raw.length > 2 && raw.length <= 7) {
      formatted = `(${raw.slice(0, 2)}) ${raw.slice(2)}`;
    } else if (raw.length > 7) {
      formatted = `(${raw.slice(0, 2)}) ${raw.slice(2, 7)}-${raw.slice(7)}`;
    }
    setTelefone(formatted);
    if (fieldErrors.telefone) {
      setFieldErrors((prev) => ({ ...prev, telefone: '' }));
    }
  };

  // Requisitos da senha
  const hasMinLength = senha.length >= 8 && senha.length <= 72;
  const hasLetterAndNumber = /^(?=.*[A-Za-z])(?=.*\d).+$/.test(senha);
  const passwordsMatch = senha.length > 0 && senha === confirmaSenha;

  // Toggle de Modalidade (Multi-seleção)
  const toggleModalidade = (modId: string) => {
    setModalidades((prev) => {
      const exists = prev.includes(modId);
      if (exists) {
        if (prev.length === 1) {
          // Mantém pelo menos uma
          return prev;
        }
        return prev.filter((item) => item !== modId);
      }
      return [...prev, modId];
    });
    if (fieldErrors.modalidades) {
      setFieldErrors((prev) => ({ ...prev, modalidades: '' }));
    }
  };

  // Validação da Etapa 1
  const validateStep1 = (): boolean => {
    const errors: Record<string, string> = {};

    if (!nome.trim()) {
      errors.nome = 'O nome completo é obrigatório.';
    } else if (nome.trim().length > 120) {
      errors.nome = 'O nome deve ter no máximo 120 caracteres.';
    }

    if (!email.trim()) {
      errors.email = 'O e-mail é obrigatório.';
    } else if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email.trim())) {
      errors.email = 'Informe um endereço de e-mail válido.';
    } else if (email.trim().length > 180) {
      errors.email = 'O e-mail deve ter no máximo 180 caracteres.';
    }

    const cleanPhone = telefone.replace(/\D/g, '');
    if (!cleanPhone) {
      errors.telefone = 'O telefone é obrigatório.';
    } else if (cleanPhone.length < 10) {
      errors.telefone = 'Informe um telefone válido com DDD (ex: 83900000000).';
    }

    if (!local.trim()) {
      errors.local = 'A localização (Cidade/UF) é obrigatória.';
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

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Validação da Etapa 2
  const validateStep2 = (): boolean => {
    const errors: Record<string, string> = {};
    if (!perfil) {
      errors.perfil = 'Selecione o seu perfil de atuação na Arena UFCG.';
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Validação da Etapa 3
  const validateStep3 = (): boolean => {
    const errors: Record<string, string> = {};

    if (!modalidades || modalidades.length === 0) {
      errors.modalidades = 'Selecione pelo menos uma modalidade esportiva.';
    }

    if (!nivelCompeticao) {
      errors.nivelCompeticao = 'Selecione o seu nível de competição.';
    }

    if (!aceiteTermos) {
      errors.aceiteTermos = 'É obrigatório aceitar os Termos de Uso e a Política de Privacidade.';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Avançar Etapa
  const handleNextStep = () => {
    setErrorMessage(null);

    if (currentStep === 1) {
      if (validateStep1()) {
        setCurrentStep(2);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    } else if (currentStep === 2) {
      if (validateStep2()) {
        setCurrentStep(3);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    }
  };

  // Voltar Etapa
  const handlePrevStep = () => {
    setErrorMessage(null);
    if (currentStep > 1) {
      setCurrentStep((prev) => prev - 1);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  // Submissão Final
  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    // Valida todas as etapas
    if (!validateStep1()) {
      setCurrentStep(1);
      return;
    }
    if (!validateStep2()) {
      setCurrentStep(2);
      return;
    }
    if (!validateStep3()) {
      return;
    }

    setIsSubmitting(true);

    // Formata telefone em apenas dígitos numéricos (ex: "83900000000")
    const cleanPhone = telefone.replace(/\D/g, '');

    try {
      await register({
        nome: nome.trim(),
        email: email.trim(),
        senha,
        confirmaSenha,
        perfil,
        telefone: cleanPhone,
        local: local.trim(),
        modalidades,
        nivelCompeticao,
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
      title="Cadastro na Arena UFCG"
      subtitle={
        currentStep === 1
          ? 'Etapa 1 de 3: Preencha seus dados de identificação e crie sua senha'
          : currentStep === 2
          ? 'Etapa 2 de 3: Escolha o seu perfil de atuação na plataforma'
          : 'Etapa 3 de 3: Selecione suas modalidades esportivas e nível de competição'
      }
      size="lg"
    >
      {/* Barra de Progresso / Stepper */}
      <StepProgress
        currentStep={currentStep}
        steps={STEPS}
        onStepClick={(stepNum) => setCurrentStep(stepNum)}
      />

      {errorMessage && (
        <Alert variant="error" onClose={() => setErrorMessage(null)} className="mb-4">
          {errorMessage}
        </Alert>
      )}

      <form onSubmit={handleSubmit} noValidate className="auth-form-multi-step">
        {/* =========================================================================
            ETAPA 1: Nome, Email, Senhas, Telefone, Local, Barra de Força da Senha
           ========================================================================= */}
        {currentStep === 1 && (
          <div className="step-pane step-pane-1">
            <div className="form-grid-2col">
              <Input
                label="Nome completo"
                type="text"
                name="nome"
                value={nome}
                onChange={(e) => {
                  setNome(e.target.value);
                  if (fieldErrors.nome) setFieldErrors({ ...fieldErrors, nome: '' });
                }}
                placeholder="Ex: João da Silva"
                leftIcon={<UserIcon />}
                error={fieldErrors.nome}
                required
                autoComplete="name"
                autoFocus
              />

              <Input
                label="E-mail"
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
            </div>

            <div className="form-grid-2col">
              <Input
                label="Telefone / WhatsApp"
                type="tel"
                name="telefone"
                value={telefone}
                onChange={(e) => handlePhoneChange(e.target.value)}
                placeholder="(83) 90000-0000"
                leftIcon={<PhoneIcon />}
                error={fieldErrors.telefone}
                helperText="Com DDD (ex: 83900000000)"
                required
                autoComplete="tel"
              />

              <Input
                label="Local (Cidade/UF)"
                type="text"
                name="local"
                value={local}
                onChange={(e) => {
                  setLocal(e.target.value);
                  if (fieldErrors.local) setFieldErrors({ ...fieldErrors, local: '' });
                }}
                placeholder="Ex: Campina Grande/PB"
                leftIcon={<MapPinIcon />}
                error={fieldErrors.local}
                required
              />
            </div>

            <div className="form-grid-2col">
              <Input
                label="Senha de acesso"
                type="password"
                name="senha"
                value={senha}
                onChange={(e) => {
                  setSenha(e.target.value);
                  if (fieldErrors.senha) setFieldErrors({ ...fieldErrors, senha: '' });
                }}
                placeholder="Mínimo 8 caracteres"
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
            </div>

            {/* Barra Indicadora da Força da Senha */}
            <PasswordStrengthBar password={senha} />

            {/* Checklist dos Requisitos da Senha */}
            <div className="password-rules-box mt-3">
              <span className="password-rules-title">Critérios de validação:</span>
              <ul className="password-rules-list">
                <li className={hasMinLength ? 'rule-met' : 'rule-unmet'}>
                  <CheckCircleIcon className="rule-icon" /> 8 a 72 caracteres
                </li>
                <li className={hasLetterAndNumber ? 'rule-met' : 'rule-unmet'}>
                  <CheckCircleIcon className="rule-icon" /> Pelo menos uma letra e um número
                </li>
                <li className={passwordsMatch ? 'rule-met' : 'rule-unmet'}>
                  <CheckCircleIcon className="rule-icon" /> Senhas coincidem
                </li>
              </ul>
            </div>

            {/* Botão de Avançar */}
            <div className="step-actions mt-4">
              <Link to="/login" className="btn btn-ghost">
                Já tenho conta
              </Link>
              <Button
                type="button"
                variant="primary"
                size="lg"
                onClick={handleNextStep}
                icon={<ArrowRightIcon />}
                iconPosition="right"
              >
                Avançar para o Perfil
              </Button>
            </div>
          </div>
        )}

        {/* =========================================================================
            ETAPA 2: Perfil selecionável em Cards com Ícone, Título e Descrição
           ========================================================================= */}
        {currentStep === 2 && (
          <div className="step-pane step-pane-2">
            <div className="step-section-header">
              <h3 className="step-section-title">Como você atuará na Arena UFCG?</h3>
              <p className="step-section-desc">
                Selecione o perfil que melhor representa a sua função principal nas competições.
              </p>
            </div>

            {fieldErrors.perfil && (
              <p className="input-error-msg mb-3">{fieldErrors.perfil}</p>
            )}

            <div className="cards-selection-grid">
              {ROLE_OPTIONS.map((item) => (
                <SelectableCard
                  key={item.role}
                  icon={item.icon}
                  title={item.title}
                  description={item.description}
                  selected={perfil === item.role}
                  onClick={() => setPerfil(item.role)}
                />
              ))}
            </div>

            <div className="step-actions mt-4">
              <Button
                type="button"
                variant="outline"
                size="md"
                onClick={handlePrevStep}
                icon={<ArrowLeftIcon />}
                iconPosition="left"
              >
                Voltar
              </Button>

              <Button
                type="button"
                variant="primary"
                size="lg"
                onClick={handleNextStep}
                icon={<ArrowRightIcon />}
                iconPosition="right"
              >
                Avançar para Esportes
              </Button>
            </div>
          </div>
        )}

        {/* =========================================================================
            ETAPA 3: Seletor de Modalidades, Nível de Competição e Termos
           ========================================================================= */}
        {currentStep === 3 && (
          <div className="step-pane step-pane-3">
            {/* Seletor de Modalidades (Multi-select Cards) */}
            <div className="step-section-header">
              <h3 className="step-section-title">Modalidades de Interesse</h3>
              <p className="step-section-desc">
                Selecione um ou mais esportes que você pratica, organiza ou acompanha (seleção múltipla).
              </p>
            </div>

            {fieldErrors.modalidades && (
              <p className="input-error-msg mb-3">{fieldErrors.modalidades}</p>
            )}

            <div className="cards-selection-grid grid-modalidades">
              {MODALIDADE_OPTIONS.map((item) => {
                const isSelected = modalidades.includes(item.id);
                return (
                  <SelectableCard
                    key={item.id}
                    icon={item.icon}
                    title={item.title}
                    description={item.description}
                    selected={isSelected}
                    multiSelect
                    onClick={() => toggleModalidade(item.id)}
                  />
                );
              })}
            </div>

            {/* Seletor de Nível de Competição (Single-select Cards) */}
            <div className="step-section-header mt-4">
              <h3 className="step-section-title">Nível de Competição</h3>
              <p className="step-section-desc">
                Defina a intensidade e nível técnico predominante das suas participações.
              </p>
            </div>

            {fieldErrors.nivelCompeticao && (
              <p className="input-error-msg mb-3">{fieldErrors.nivelCompeticao}</p>
            )}

            <div className="cards-selection-grid grid-niveis">
              {NIVEL_OPTIONS.map((item) => (
                <SelectableCard
                  key={item.nivel}
                  icon={item.icon}
                  title={item.title}
                  description={item.description}
                  badge={item.badge}
                  selected={nivelCompeticao === item.nivel}
                  onClick={() => setNivelCompeticao(item.nivel)}
                />
              ))}
            </div>

            {/* Caixa seletora de aceitar os termos */}
            <div className="terms-acceptance-box mt-4">
              <Checkbox
                name="aceiteTermos"
                checked={aceiteTermos}
                onChange={(e) => {
                  setAceiteTermos(e.target.checked);
                  if (fieldErrors.aceiteTermos) {
                    setFieldErrors((prev) => ({ ...prev, aceiteTermos: '' }));
                  }
                }}
                error={fieldErrors.aceiteTermos}
                required
                label={
                  <span>
                    Li e concordo com os{' '}
                    <a
                      href="#termos"
                      onClick={(e) => {
                        e.preventDefault();
                        alert(
                          'Termos de Uso e Política de Privacidade da Arena UFCG:\n\n1. O uso da plataforma destina-se a fins esportivos e acadêmicos na UFCG.\n2. Seus dados estão seguros e não serão compartilhados com terceiros.'
                        );
                      }}
                      className="auth-text-link"
                    >
                      Termos de Uso
                    </a>{' '}
                    e a{' '}
                    <a
                      href="#privacidade"
                      onClick={(e) => {
                        e.preventDefault();
                        alert('Política de Privacidade da Arena UFCG: Respeitamos a LGPD e a segurança dos seus dados.');
                      }}
                      className="auth-text-link"
                    >
                      Política de Privacidade
                    </a>.
                  </span>
                }
              />
            </div>

            {/* Ações da Etapa 3 */}
            <div className="step-actions mt-4">
              <Button
                type="button"
                variant="outline"
                size="md"
                onClick={handlePrevStep}
                disabled={isSubmitting}
                icon={<ArrowLeftIcon />}
                iconPosition="left"
              >
                Voltar
              </Button>

              <Button
                type="submit"
                variant="primary"
                size="lg"
                isLoading={isSubmitting}
                icon={<CheckCircleIcon />}
                iconPosition="right"
              >
                {isSubmitting ? 'Cadastrando...' : 'Concluir Cadastro'}
              </Button>
            </div>
          </div>
        )}
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
