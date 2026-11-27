package com.esproject4.arenaufcg.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esproject4.arenaufcg.Model.User;
import com.esproject4.arenaufcg.Model.UserRole;
import com.esproject4.arenaufcg.Model.ValueObjects.Email;
import com.esproject4.arenaufcg.Service.security.NotificacaoService;
import com.esproject4.arenaufcg.Service.security.PasswordHasher;
import com.esproject4.arenaufcg.Service.security.TokenClaims;
import com.esproject4.arenaufcg.Service.security.TokenProvider;
import com.esproject4.arenaufcg.DTO.auth.LoginRequestDTO;
import com.esproject4.arenaufcg.DTO.auth.RegisterRequestDTO;
import com.esproject4.arenaufcg.DTO.UserResponseDTO;
import com.esproject4.arenaufcg.DTO.auth.AuthResponseDTO;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Duration VALIDADE_CODIGO_VERIFICACAO = Duration.ofHours(24);
    private static final Duration VALIDADE_TOKEN_REDEFINICAO = Duration.ofMinutes(30);
    // 8 a 72 caracteres (limite do BCrypt), com ao menos uma letra e um número.
    private static final Pattern POLITICA_SENHA = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final NotificacaoService notificacaoService;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (!Boolean.TRUE.equals(dto.aceiteTermos())) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "TERMOS_NAO_ACEITOS",
                    "É necessário aceitar os Termos de Uso e a Política de Privacidade.");
        }

        if (dto.perfil() == null || dto.perfil() == UserRole.ADMIN) {
            throw new RegraNegocioException(
                HttpStatus.BAD_REQUEST,
                "PERFIL_INVALIDO",
                "O perfil informado não é permitido no cadastro público."
        );
}
        validarSenha(dto.senha(), dto.confirmaSenha());

        Email email = criarEmail(dto.email());
        if (userRepository.existsByEmailAddress(email.address())) {
            throw emailJaCadastrado();
        }

        String codigo = gerarCodigoVerificacao();
        User user = User.builder()
                .name(dto.nome())
                .email(email)
                .password(passwordHasher.hash(dto.senha()))
                .role(dto.perfil())
                .acceptedTerms(true)
                .verifyToken(codigo)
                .verifyTokenExpiresIn(OffsetDateTime.now().plus(VALIDADE_CODIGO_VERIFICACAO))
                .build();

        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // Duas requisições simultâneas com o mesmo e-mail: a constraint unique decide.
            throw emailJaCadastrado();
        }

        notificacaoService.enviarCodigoVerificacao(email.address(), codigo);
        return montarResposta(user);
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO dto) {
        // Mesma resposta para e-mail inexistente e senha errada (não revela quais
        // e-mails existem).
        User user = buscarPorEmail(dto.email()).orElseThrow(this::credenciaisInvalidas);

        if (!passwordHasher.verificar(dto.senha(), user.getPassword())) {
            throw credenciaisInvalidas();
        }
        // Só depois de provar a senha é que revelamos o estado da conta.
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new RegraNegocioException(HttpStatus.FORBIDDEN, "CONTA_INATIVA",
                    "Esta conta está desativada.");
        }
        if (!Boolean.TRUE.equals(user.getVerified())) {
            throw new RegraNegocioException(HttpStatus.FORBIDDEN, "CONTA_NAO_VERIFICADA",
                    "Verifique seu e-mail antes de entrar.");
        }

        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);
        return montarResposta(user);
    }

    @Transactional
    public void verifyAccount(String token, String userEmail) {
        User user = buscarPorEmail(userEmail).orElseThrow(this::codigoInvalido);

        if (Boolean.TRUE.equals(user.getVerified())) {
            throw new RegraNegocioException(HttpStatus.CONFLICT, "CONTA_JA_VERIFICADA",
                    "Esta conta já foi verificada.");
        }
        if (token == null || user.getVerifyToken() == null
                || !iguaisEmTempoConstante(token, user.getVerifyToken())) {
            throw codigoInvalido();
        }
        if (user.getVerifyTokenExpiresIn() == null
                || user.getVerifyTokenExpiresIn().isBefore(OffsetDateTime.now())) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "CODIGO_EXPIRADO",
                    "O código de verificação expirou.");
        }

        user.setVerified(true);
        user.setVerifyToken(null);
        user.setVerifyTokenExpiresIn(null);
        userRepository.save(user);
    }

    // Silencioso de propósito: responde igual exista ou não o e-mail (evita
    // enumeração de usuários).
    @Transactional
    public void forgotPassword(String userEmail) {
        buscarPorEmail(userEmail)
                .filter(u -> Boolean.TRUE.equals(u.getActive()))
                .ifPresent(user -> {
                    String token = gerarTokenAleatorio();
                    // No banco vai só o hash do token; o token em si segue por e-mail.
                    user.setResetPasswordToken(sha256(token));
                    user.setResetPasswordTokenExpiresIn(
                            OffsetDateTime.now().plus(VALIDADE_TOKEN_REDEFINICAO));
                    userRepository.save(user);
                    notificacaoService.enviarRecuperacaoDeSenha(user.getEmail().address(), token);
                });
    }

    @Transactional
    public void resetPassword(String password, String confirmPassword, String resetToken) {
        validarSenha(password, confirmPassword);

        if (resetToken == null || resetToken.isBlank()) {
            throw tokenRedefinicaoInvalido();
        }
        User user = userRepository.findByResetPasswordToken(sha256(resetToken))
                .orElseThrow(this::tokenRedefinicaoInvalido);

        if (user.getResetPasswordTokenExpiresIn() == null
                || user.getResetPasswordTokenExpiresIn().isBefore(OffsetDateTime.now())) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "TOKEN_EXPIRADO",
                    "O token de redefinição expirou.");
        }

        user.setPassword(passwordHasher.hash(password));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiresIn(null);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getSessionUser(String bearerToken) {
        if (bearerToken == null || !bearerToken.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw tokenSessaoInvalido();
        }
        TokenClaims claims = tokenProvider.validarToken(bearerToken.substring(7).trim());

        User user = userRepository.findById(claims.userId())
                .filter(u -> Boolean.TRUE.equals(u.getActive()))
                .orElseThrow(this::tokenSessaoInvalido);
        return UserResponseDTO.from(user);
    }

    private AuthResponseDTO montarResposta(User user) {
        return AuthResponseDTO.of(tokenProvider.gerarToken(user), UserResponseDTO.from(user));
    }

    private void validarSenha(String senha, String confirmaSenha) {
        if (senha == null || !POLITICA_SENHA.matcher(senha).matches()) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "SENHA_FRACA",
                    "A senha deve ter de 8 a 72 caracteres, com letras e números.");
        }
        if (!senha.equals(confirmaSenha)) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "SENHAS_DIFERENTES",
                    "As senhas não conferem.");
        }
    }

    private Email criarEmail(String valor) {
        try {
            return Email.of(valor);
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "EMAIL_INVALIDO",
                    "E-mail inválido.");
        }
    }

    private Optional<User> buscarPorEmail(String valor) {
        Email email;
        try {
            email = Email.of(valor);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        return userRepository.findByEmailAddress(email.address());
    }

    private String gerarCodigoVerificacao() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String gerarTokenAleatorio() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String valor) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    private static boolean iguaisEmTempoConstante(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private RegraNegocioException credenciaisInvalidas() {
        return new RegraNegocioException(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS",
                "E-mail ou senha inválidos.");
    }

    private RegraNegocioException emailJaCadastrado() {
        return new RegraNegocioException(HttpStatus.CONFLICT, "EMAIL_JA_CADASTRADO",
                "Este e-mail já está cadastrado.");
    }

    private RegraNegocioException codigoInvalido() {
        return new RegraNegocioException(HttpStatus.BAD_REQUEST, "CODIGO_INVALIDO",
                "Código de verificação inválido ou expirado.");
    }

    private RegraNegocioException tokenRedefinicaoInvalido() {
        return new RegraNegocioException(HttpStatus.BAD_REQUEST, "TOKEN_INVALIDO",
                "Token de redefinição inválido ou expirado.");
    }

    private RegraNegocioException tokenSessaoInvalido() {
        return new RegraNegocioException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO",
                "Token ausente, malformado ou expirado.");
    }
}
