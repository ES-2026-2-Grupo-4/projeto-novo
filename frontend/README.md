# Arena UFCG - Frontend

Interface web do sistema de gestão esportiva **Arena UFCG**, desenvolvida com **React 19**, **TypeScript** e **Vite**, integrada a uma API REST com controle completo de autenticação e proteção de rotas.

---

## 🚀 Funcionalidades Implementadas

### 1. Sistema Completo de Autenticação
- **Cadastro (`/cadastro` ou `/register`)**:
  - Campos: Nome completo, E-mail, Perfil de acesso (`ORGANIZADOR`, `DIRIGENTE`, `ARBITRO`, `ESPECTADOR`), Senha, Confirmação de Senha e Aceite dos Termos de Uso.
  - Validação em tempo real dos requisitos de senha (8 a 72 caracteres, letras e números, confirmação).
- **Login (`/login`)**:
  - Campos: E-mail e Senha.
  - Opção para exibir/ocultar senha.
  - Links para recuperação de senha e criação de conta.
  - Armazenamento seguro de token JWT / Bearer e redirecionamento para o destino desejado.
- **Recuperação de Senha (`/esqueci-senha` ou `/forgot-password`)**:
  - Formulário para envio de e-mail de recuperação.
- **Redefinição de Senha (`/redefinir-senha` e `/redefinir-senha/:token`)**:
  - Permite definir uma nova senha via token enviado na URL ou digitado manualmente.
- **Verificação de Conta (`/verificar-conta`)**:
  - Confirmação de código numérico de 6 dígitos enviado por e-mail.

### 2. Proteção de Rotas e Controle de Acesso
- **`ProtectedRoute`**: Bloqueia acesso a páginas restritas para usuários não autenticados, redirecionando para `/login` e guardando a URL de origem para retorno automático após autenticação.
- **`PublicRoute`**: Evita que usuários já logados acessem telas de login/cadastro novamente, direcionando-os ao Dashboard.
- **Expiração de Sessão (401)**: Intercepta respostas de token inválido ou expirado, limpando a sessão e redirecionando para login.

### 3. Integração com a API REST
- Cliente HTTP tipado (`apiClient`) configurável via variável de ambiente `VITE_API_BASE_URL` (com proxy configurado no Vite para `http://localhost:8080`).
- Injeção automática do cabeçalho `Authorization: Bearer <token>`.
- Endpoints integrados:
  - `POST /api/v1/auth/register`
  - `POST /api/v1/auth/login`
  - `GET /api/v1/auth/me`
  - `POST /api/v1/auth/forgot-password`
  - `POST /api/v1/auth/reset-password/{token}`
  - `POST /api/v1/auth/verify`

---

## 📁 Estrutura de Pastas

```
frontend/src/
├── api/
│   ├── client.ts                 # Cliente HTTP fetch com Bearer token e tratamento de erros
│   └── authApi.ts                # Chamadas aos endpoints REST da API
├── components/
│   ├── common/
│   │   ├── Alert.tsx             # Componente de feedback (sucesso, erro, alerta, info)
│   │   ├── Button.tsx            # Botão personalizável (variantes, tamanhos, loading spinner)
│   │   ├── Card.tsx              # Cartões de superfície
│   │   ├── Checkbox.tsx          # Checkbox estilizado acessível
│   │   ├── Icons.tsx             # Conjunto de ícones SVG limpos e leves
│   │   ├── Input.tsx             # Campo de texto com label, ícones, validação e toggle de senha
│   │   └── Select.tsx            # Dropdown estilizado
│   └── layout/
│       ├── AppLayout.tsx         # Estrutura com Navbar para páginas autenticadas
│       ├── AuthLayout.tsx        # Estrutura em card com identidade Arena UFCG
│       └── Navbar.tsx            # Barra de navegação com dados do usuário e logout
├── context/
│   ├── authContextInstance.ts    # Instância do React Context
│   ├── AuthContext.tsx           # Provedor global de autenticação
│   └── useAuth.ts                # Hook customizado useAuth()
├── pages/
│   ├── auth/
│   │   ├── LoginPage.tsx         # Página de login
│   │   ├── RegisterPage.tsx      # Página de cadastro
│   │   ├── ForgotPasswordPage.tsx# Solicitação de recuperação de senha
│   │   ├── ResetPasswordPage.tsx # Redefinição de senha com token
│   │   └── VerifyAccountPage.tsx # Confirmação de código de 6 dígitos
│   ├── dashboard/
│   │   └── DashboardPage.tsx     # Área restrita com testes HTTP e ações por perfil
│   ├── profile/
│   │   └── ProfilePage.tsx       # Perfil completo do usuário
│   ├── NotFoundPage.tsx          # 404
│   └── UnauthorizedPage.tsx      # 403
├── routes/
│   ├── ProtectedRoute.tsx        # Guarda de rotas protegidas
│   └── PublicRoute.tsx           # Guarda de rotas públicas/convidado
├── types/
│   └── auth.ts                   # Interfaces TypeScript completas
├── App.tsx                       # Roteamento da aplicação
├── main.tsx                      # Ponto de entrada
└── index.css                     # Design system e estilos responsivos
```

---

## 🛠️ Como Executar

### 1. Instalar dependências
```bash
npm install
```

### 2. Configurar variáveis de ambiente (opcional)
Copie o arquivo `.env.example` para `.env`:
```bash
cp .env.example .env
```

### 3. Rodar em modo de desenvolvimento
```bash
npm run dev
```
Acesse `http://localhost:5173`. O Vite já possui proxy configurado para redirecionar chamadas de `/api` para a API Spring Boot em `http://localhost:8080`.

### 4. Executar checagens de tipos e linter
```bash
npm run lint
npm run build
```
