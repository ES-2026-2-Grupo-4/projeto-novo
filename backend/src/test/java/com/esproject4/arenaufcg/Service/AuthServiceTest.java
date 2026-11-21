package com.esproject4.arenaufcg.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.DTO.auth.AuthResponseDTO;
import com.esproject4.arenaufcg.DTO.auth.LoginRequestDTO;
import com.esproject4.arenaufcg.DTO.auth.RegisterRequestDTO;
import com.esproject4.arenaufcg.DTO.UserResponseDTO;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.User;
import com.esproject4.arenaufcg.Model.UserRole;
import com.esproject4.arenaufcg.Model.ValueObjects.Email;
import com.esproject4.arenaufcg.Repository.UserRepository;
import com.esproject4.arenaufcg.Service.security.NotificacaoService;
import com.esproject4.arenaufcg.Service.security.PasswordHasher;
import com.esproject4.arenaufcg.Service.security.TokenClaims;
import com.esproject4.arenaufcg.Service.security.TokenProvider;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordHasher passwordHasher;
    @Mock private TokenProvider tokenProvider;
    @Mock private NotificacaoService notificacaoService;

    @InjectMocks private AuthService authService;

    private static final String SENHA_VALIDA = "Senha123";
    private static final String HASH = "$2a$10$hashfake";

    // ---------- helpers ----------

    private User usuarioValido() {
        return User.builder()
                .id(UUID.randomUUID())
                .name("Maria Souza")
                .email(Email.of("maria@exemplo.com"))
                .password(HASH)
                .role(UserRole.ORGANIZADOR)
                .verified(true)
                .active(true)
                .acceptedTerms(true)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    private RegisterRequestDTO registro(String email, String senha, String confirma,
                                        UserRole perfil, Boolean termos) {
        return new RegisterRequestDTO("Maria Souza", email, senha, confirma, perfil, termos);
    }

    private RegisterRequestDTO registroValido() {
        return registro("maria@exemplo.com", SENHA_VALIDA, SENHA_VALIDA, UserRole.ORGANIZADOR, true);
    }

    private RegraNegocioException erroDe(ThrowingCallable call) {
        Throwable t = catchThrowable(call);
        assertThat(t).isInstanceOf(RegraNegocioException.class);
        return (RegraNegocioException) t;
    }

    // ---------- register ----------

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("deve cadastrar usuário com sucesso, com senha hasheada e código enviado")
        void deveCadastrarComSucesso() {
            when(userRepository.existsByEmailAddress("maria@exemplo.com")).thenReturn(false);
            when(passwordHasher.hash(SENHA_VALIDA)).thenReturn(HASH);
            when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                u.setCreatedAt(OffsetDateTime.now());
                return u;
            });
            when(tokenProvider.gerarToken(any(User.class))).thenReturn("jwt-token");

            AuthResponseDTO resposta = authService.register(registroValido());

            assertThat(resposta.accessToken()).isEqualTo("jwt-token");
            assertThat(resposta.usuario().email()).isEqualTo("maria@exemplo.com");
            verify(passwordHasher).hash(SENHA_VALIDA);
            verify(notificacaoService).enviarCodigoVerificacao(eqEmail(), anyString());
        }

        @Test
        @DisplayName("deve lançar 409 ao cadastrar com e-mail já existente")
        void deveFalharEmailJaExistente() {
            when(userRepository.existsByEmailAddress(anyString())).thenReturn(true);

            RegraNegocioException ex = erroDe(() -> authService.register(registroValido()));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(ex.getCodigo()).isEqualTo("EMAIL_JA_CADASTRADO");
            verify(userRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("deve lançar 400 ao cadastrar sem aceitar os termos")
        void deveFalharSemAceitarTermos() {
            RegraNegocioException ex = erroDe(() -> authService.register(
                    registro("maria@exemplo.com", SENHA_VALIDA, SENHA_VALIDA, UserRole.ORGANIZADOR, false)));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getCodigo()).isEqualTo("TERMOS_NAO_ACEITOS");
        }

        @Test
        @DisplayName("deve lançar 400 quando a senha é fraca")
        void deveFalharSenhaFraca() {
            RegraNegocioException ex = erroDe(() -> authService.register(
                    registro("maria@exemplo.com", "123", "123", UserRole.ORGANIZADOR, true)));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getCodigo()).isEqualTo("SENHA_FRACA");
        }

        @Test
        @DisplayName("deve lançar 400 quando as senhas não conferem")
        void deveFalharSenhasDiferentes() {
            RegraNegocioException ex = erroDe(() -> authService.register(
                    registro("maria@exemplo.com", SENHA_VALIDA, "Outra123", UserRole.ORGANIZADOR, true)));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getCodigo()).isEqualTo("SENHAS_DIFERENTES");
        }


        @Test
        @DisplayName("deve lançar 400 quando o e-mail é inválido")
        void deveFalharEmailInvalido() {
            RegraNegocioException ex = erroDe(() -> authService.register(
                    registro("email-invalido", SENHA_VALIDA, SENHA_VALIDA, UserRole.ORGANIZADOR, true)));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getCodigo()).isEqualTo("EMAIL_INVALIDO");
        }

        @Test
        @DisplayName("deve tratar corrida de e-mail duplicado (constraint unique) como 409")
        void deveTratarViolacaoDeIntegridade() {
            when(userRepository.existsByEmailAddress(anyString())).thenReturn(false);
            when(passwordHasher.hash(anyString())).thenReturn(HASH);
            when(userRepository.saveAndFlush(any(User.class)))
                    .thenThrow(new DataIntegrityViolationException("unique"));

            RegraNegocioException ex = erroDe(() -> authService.register(registroValido()));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(ex.getCodigo()).isEqualTo("EMAIL_JA_CADASTRADO");
        }

		@Test
		@DisplayName("deve lançar 400 quando o perfil é nulo")
		void deveFalharPerfilNulo() {
			RegraNegocioException ex = erroDe(() -> authService.register(
						registro("outro@exemplo.com", SENHA_VALIDA, SENHA_VALIDA, null, true)));

			assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(ex.getCodigo()).isEqualTo("PERFIL_INVALIDO");
		}
    }

    // ---------- login ----------

    @Nested
    @DisplayName("login")
    class Login {

        private LoginRequestDTO login(String email, String senha) {
            return new LoginRequestDTO(email, senha);
        }

        @Test
        @DisplayName("deve autenticar, atualizar o último login e retornar token")
        void deveAutenticarComSucesso() {
            User user = usuarioValido();
            when(userRepository.findByEmailAddress("maria@exemplo.com")).thenReturn(Optional.of(user));
            when(passwordHasher.verificar(SENHA_VALIDA, HASH)).thenReturn(true);
            when(tokenProvider.gerarToken(user)).thenReturn("jwt-token");

            AuthResponseDTO resposta = authService.login(login("maria@exemplo.com", SENHA_VALIDA));

            assertThat(resposta.accessToken()).isEqualTo("jwt-token");
            assertThat(user.getLastLoginAt()).isNotNull();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("deve lançar 401 quando o e-mail não existe")
        void deveFalharEmailInexistente() {
            when(userRepository.findByEmailAddress(anyString())).thenReturn(Optional.empty());

            RegraNegocioException ex = erroDe(() -> authService.login(login("x@exemplo.com", SENHA_VALIDA)));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(ex.getCodigo()).isEqualTo("CREDENCIAIS_INVALIDAS");
        }

        @Test
        @DisplayName("deve lançar 401 quando a senha está incorreta")
        void deveFalharSenhaIncorreta() {
            User user = usuarioValido();
            when(userRepository.findByEmailAddress(anyString())).thenReturn(Optional.of(user));
            when(passwordHasher.verificar(anyString(), anyString())).thenReturn(false);

            RegraNegocioException ex = erroDe(() -> authService.login(login("maria@exemplo.com", "errada1")));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(ex.getCodigo()).isEqualTo("CREDENCIAIS_INVALIDAS");
        }

        @Test
        @DisplayName("deve lançar 403 quando a conta está inativa")
        void deveFalharContaInativa() {
            User user = usuarioValido();
            user.setActive(false);
            when(userRepository.findByEmailAddress(anyString())).thenReturn(Optional.of(user));
            when(passwordHasher.verificar(anyString(), anyString())).thenReturn(true);

            RegraNegocioException ex = erroDe(() -> authService.login(login("maria@exemplo.com", SENHA_VALIDA)));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(ex.getCodigo()).isEqualTo("CONTA_INATIVA");
        }

        @Test
        @DisplayName("deve lançar 403 quando a conta não foi verificada")
        void deveFalharContaNaoVerificada() {
            User user = usuarioValido();
            user.setVerified(false);
            when(userRepository.findByEmailAddress(anyString())).thenReturn(Optional.of(user));
            when(passwordHasher.verificar(anyString(), anyString())).thenReturn(true);

            RegraNegocioException ex = erroDe(() -> authService.login(login("maria@exemplo.com", SENHA_VALIDA)));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(ex.getCodigo()).isEqualTo("CONTA_NAO_VERIFICADA");
        }
    }

    // ---------- verifyAccount ----------

    @Nested
    @DisplayName("verifyAccount")
    class VerifyAccount {

        @Test
        @DisplayName("deve verificar a conta com código válido e limpar o token")
        void deveVerificarComSucesso() {
            User user = usuarioValido();
            user.setVerified(false);
            user.setVerifyToken("123456");
            user.setVerifyTokenExpiresIn(OffsetDateTime.now().plusHours(1));
            when(userRepository.findByEmailAddress(anyString())).thenReturn(Optional.of(user));

            authService.verifyAccount("123456", "maria@exemplo.com");

            assertThat(user.getVerified()).isTrue();
            assertThat(user.getVerifyToken()).isNull();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("deve lançar 409 quando a conta já está verificada")
        void deveFalharJaVerificada() {
            User user = usuarioValido();
            user.setVerified(true);
            when(userRepository.findByEmailAddress(anyString())).thenReturn(Optional.of(user));

            RegraNegocioException ex = erroDe(() -> authService.verifyAccount("123456", "maria@exemplo.com"));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(ex.getCodigo()).isEqualTo("CONTA_JA_VERIFICADA");
        }

        @Test
        @DisplayName("deve lançar 400 quando o código é inválido")
        void deveFalharCodigoInvalido() {
            User user = usuarioValido();
            user.setVerified(false);
            user.setVerifyToken("123456");
            user.setVerifyTokenExpiresIn(OffsetDateTime.now().plusHours(1));
            when(userRepository.findByEmailAddress(anyString())).thenReturn(Optional.of(user));

            RegraNegocioException ex = erroDe(() -> authService.verifyAccount("000000", "maria@exemplo.com"));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getCodigo()).isEqualTo("CODIGO_INVALIDO");
        }

        @Test
        @DisplayName("deve lançar 400 quando o código expirou")
        void deveFalharCodigoExpirado() {
            User user = usuarioValido();
            user.setVerified(false);
            user.setVerifyToken("123456");
            user.setVerifyTokenExpiresIn(OffsetDateTime.now().minusMinutes(1));
            when(userRepository.findByEmailAddress(anyString())).thenReturn(Optional.of(user));

            RegraNegocioException ex = erroDe(() -> authService.verifyAccount("123456", "maria@exemplo.com"));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getCodigo()).isEqualTo("CODIGO_EXPIRADO");
        }
    }

    // ---------- forgotPassword ----------

    @Nested
    @DisplayName("forgotPassword")
    class ForgotPassword {

        @Test
        @DisplayName("deve gerar token de reset e enviar e-mail para usuário ativo")
        void deveGerarTokenParaUsuarioAtivo() {
            User user = usuarioValido();
            when(userRepository.findByEmailAddress(anyString())).thenReturn(Optional.of(user));

            authService.forgotPassword("maria@exemplo.com");

            assertThat(user.getResetPasswordToken()).isNotNull();
            assertThat(user.getResetPasswordTokenExpiresIn()).isNotNull();
            verify(userRepository).save(user);
            verify(notificacaoService).enviarRecuperacaoDeSenha(eqEmail(), anyString());
        }

        @Test
        @DisplayName("não deve revelar se o e-mail existe quando ele não está cadastrado")
        void naoDeveRevelarEmailInexistente() {
            when(userRepository.findByEmailAddress(anyString())).thenReturn(Optional.empty());

            authService.forgotPassword("naoexiste@exemplo.com");

            verify(userRepository, never()).save(any());
            verify(notificacaoService, never()).enviarRecuperacaoDeSenha(anyString(), anyString());
        }
    }

    // ---------- resetPassword ----------

    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        @Test
        @DisplayName("deve redefinir a senha com token válido e limpar o token")
        void deveRedefinirComSucesso() {
            User user = usuarioValido();
            user.setResetPasswordToken("hash-token");
            user.setResetPasswordTokenExpiresIn(OffsetDateTime.now().plusMinutes(10));
            when(userRepository.findByResetPasswordToken(anyString())).thenReturn(Optional.of(user));
            when(passwordHasher.hash(SENHA_VALIDA)).thenReturn("novo-hash");

            authService.resetPassword(SENHA_VALIDA, SENHA_VALIDA, "token-plano");

            assertThat(user.getPassword()).isEqualTo("novo-hash");
            assertThat(user.getResetPasswordToken()).isNull();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("deve lançar 400 quando o token de reset é inválido")
        void deveFalharTokenInvalido() {
            when(userRepository.findByResetPasswordToken(anyString())).thenReturn(Optional.empty());

            RegraNegocioException ex = erroDe(() ->
                    authService.resetPassword(SENHA_VALIDA, SENHA_VALIDA, "token-qualquer"));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getCodigo()).isEqualTo("TOKEN_INVALIDO");
        }

        @Test
        @DisplayName("deve lançar 400 quando o token de reset expirou")
        void deveFalharTokenExpirado() {
            User user = usuarioValido();
            user.setResetPasswordToken("hash-token");
            user.setResetPasswordTokenExpiresIn(OffsetDateTime.now().minusMinutes(1));
            when(userRepository.findByResetPasswordToken(anyString())).thenReturn(Optional.of(user));

            RegraNegocioException ex = erroDe(() ->
                    authService.resetPassword(SENHA_VALIDA, SENHA_VALIDA, "token-plano"));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getCodigo()).isEqualTo("TOKEN_EXPIRADO");
        }

        @Test
        @DisplayName("deve lançar 400 quando a nova senha é fraca")
        void deveFalharSenhaFraca() {
            RegraNegocioException ex = erroDe(() ->
                    authService.resetPassword("123", "123", "token-plano"));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getCodigo()).isEqualTo("SENHA_FRACA");
        }

		@Test
		@DisplayName("não deve enviar e-mail quando a conta está inativa")
		void naoDeveEnviarParaContaInativa() {
			User user = usuarioValido();
			user.setActive(false);
			when(userRepository.findByEmailAddress(anyString())).thenReturn(Optional.of(user));

			authService.forgotPassword("maria@exemplo.com");

			verify(userRepository, never()).save(any());
			verify(notificacaoService, never()).enviarRecuperacaoDeSenha(anyString(), anyString());
		}
    }

    // ---------- getSessionUser ----------

    @Nested
    @DisplayName("getSessionUser")
    class GetSessionUser {

        @Test
        @DisplayName("deve retornar o usuário da sessão com token válido")
        void deveRetornarUsuarioComTokenValido() {
            User user = usuarioValido();
            when(tokenProvider.validarToken("abc"))
                    .thenReturn(new TokenClaims(user.getId(), "maria@exemplo.com", UserRole.ORGANIZADOR));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            UserResponseDTO dto = authService.getSessionUser("Bearer abc");

            assertThat(dto.email()).isEqualTo("maria@exemplo.com");
        }

        @Test
        @DisplayName("deve lançar 401 quando o header não é um Bearer")
        void deveFalharSemBearer() {
            RegraNegocioException ex = erroDe(() -> authService.getSessionUser("abc"));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(ex.getCodigo()).isEqualTo("TOKEN_INVALIDO");
        }

        @Test
        @DisplayName("deve propagar erro quando o token é inválido")
        void deveFalharTokenInvalido() {
            when(tokenProvider.validarToken(anyString()))
                    .thenThrow(new RegraNegocioException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO", "x"));

            RegraNegocioException ex = erroDe(() -> authService.getSessionUser("Bearer ruim"));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(ex.getCodigo()).isEqualTo("TOKEN_INVALIDO");
        }

        @Test
        @DisplayName("deve lançar 401 quando o usuário do token não existe mais")
        void deveFalharUsuarioInexistente() {
            UUID id = UUID.randomUUID();
            when(tokenProvider.validarToken(anyString()))
                    .thenReturn(new TokenClaims(id, "maria@exemplo.com", UserRole.ORGANIZADOR));
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            RegraNegocioException ex = erroDe(() -> authService.getSessionUser("Bearer abc"));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(ex.getCodigo()).isEqualTo("TOKEN_INVALIDO");
        }


    }

    // matcher utilitário para o e-mail normalizado
    private static String eqEmail() {
        return org.mockito.ArgumentMatchers.eq("maria@exemplo.com");
    }
}
