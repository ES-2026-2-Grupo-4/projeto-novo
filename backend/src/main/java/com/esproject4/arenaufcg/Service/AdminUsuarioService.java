package com.esproject4.arenaufcg.Service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.esproject4.arenaufcg.DTO.AdminUsuarioResponseDTO;
import com.esproject4.arenaufcg.DTO.RegistroLogCommand;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.User;
import com.esproject4.arenaufcg.Model.UserRole;
import com.esproject4.arenaufcg.Model.ValueObjects.Email;
import com.esproject4.arenaufcg.Repository.UserRepository;
import com.esproject4.arenaufcg.Service.security.PasswordHasher;

@Service
public class AdminUsuarioService {

    private final UserRepository repository;
    private final PasswordHasher passwordHasher;
    private final AuditoriaService auditoriaService;
    private final AuthService authService;

    public AdminUsuarioService(
            UserRepository repository,
            PasswordHasher passwordHasher,
            AuditoriaService auditoriaService,
            AuthService authService) {

        this.repository = repository;
        this.passwordHasher = passwordHasher;
        this.auditoriaService = auditoriaService;
        this.authService = authService;
    }

    @Transactional
    AdminUsuarioResponseDTO criarAdministrador(
            String nome,
            String email,
            String senha) {

        validarNome(nome);
        validarSenha(senha);

        String emailNormalizado = normalizarEmail(email);
        Email emailValido = validarEmail(emailNormalizado);

        if (repository.existsByEmailAddress(emailNormalizado)) {
            throw emailJaCadastrado();
        }

        User administrador = User.criarAdministradorViaCli(
            nome.trim(),
            emailValido,
            passwordHasher.hash(senha)
        );

        try {
            administrador = repository.saveAndFlush(administrador);
        } catch (DataIntegrityViolationException exception) {
            throw emailJaCadastrado();
        }

        registrarAuditoria(
            "ADMIN_CRIAR",
            "Administrador criado: " + emailNormalizado,
            201
        );

        return AdminUsuarioResponseDTO.de(administrador);
    }

    @Transactional
    void desativarAdministrador(String email) {

        String emailNormalizado = normalizarEmail(email);
        validarEmail(emailNormalizado);

        repository.buscarPorPerfilComBloqueio(UserRole.ADMIN);

        User administrador = repository
            .findByEmailAddress(emailNormalizado)
            .filter(user -> user.getRole() == UserRole.ADMIN)
            .filter(user -> Boolean.TRUE.equals(user.getActive()))
            .orElseThrow(() -> new RegraNegocioException(
                HttpStatus.NOT_FOUND,
                "ADMIN_NAO_ENCONTRADO",
                "Administrador ativo não encontrado."
            ));

        if (repository.countByRoleAndActiveTrue(UserRole.ADMIN) <= 1) {
            throw new RegraNegocioException(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "ULTIMO_ADMIN_ATIVO",
                "O último administrador ativo não pode ser desativado."
            );
        }

        administrador.desativar();

        registrarAuditoria(
            "ADMIN_DESATIVAR",
            "Administrador desativado: " + emailNormalizado,
            200
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminUsuarioResponseDTO> listarAdministradores(
            String authorization,
            Pageable pageable) {

        var usuario = authService.getSessionUser(authorization);

        if (usuario.perfil() != UserRole.ADMIN) {
            throw new RegraNegocioException(
                HttpStatus.FORBIDDEN,
                "ACESSO_NEGADO",
                "Esta operação é exclusiva de administradores."
            );
        }

        return repository
            .findByRoleAndActiveTrue(UserRole.ADMIN, pageable)
            .map(AdminUsuarioResponseDTO::de);
    }

    // Métodos auxiliares

    private void validarNome(String nome) {

        if (nome == null || nome.isBlank()
                || nome.trim().length() > 120) {

            throw new RegraNegocioException(
                HttpStatus.BAD_REQUEST,
                "NOME_INVALIDO",
                "Informe um nome válido."
            );
        }
    }

    private void validarSenha(String senha) {

        if (senha == null
                || senha.isBlank()
                || senha.getBytes(StandardCharsets.UTF_8).length > 72
                || !senha.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$")) {

            throw new RegraNegocioException(
                HttpStatus.BAD_REQUEST,
                "SENHA_FRACA",
                "A senha deve conter letras e números, com no mínimo 8 caracteres e no máximo 72 bytes."
            );
        }
    }

    private String normalizarEmail(String email) {
        return email == null
            ? null
            : email.trim().toLowerCase(Locale.ROOT);
    }

    private Email validarEmail(String email) {

        try {
            if (email == null || email.length() > 180) {
                throw new IllegalArgumentException();
            }

            return Email.of(email);

        } catch (IllegalArgumentException exception) {

            throw new RegraNegocioException(
                HttpStatus.BAD_REQUEST,
                "EMAIL_INVALIDO",
                "Informe um e-mail válido."
            );
        }
    }

    private RegraNegocioException emailJaCadastrado() {
        return new RegraNegocioException(
            HttpStatus.CONFLICT,
            "EMAIL_JA_CADASTRADO",
            "O e-mail informado já está cadastrado."
        );
    }

    private void registrarAuditoria(
            String acao,
            String detalhes,
            int status) {

        RegistroLogCommand comando = new RegistroLogCommand(
            null,
            null,
            UserRole.ADMIN,
            acao,
            "cli://admin-provisioning",
            "CLI",
            status,
            detalhes,
            null
        );

        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    auditoriaService.registrarLog(comando);
                }
            }
        );
    }
}