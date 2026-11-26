package com.esproject4.arenaufcg.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.esproject4.arenaufcg.DTO.AtualizarPermissoesRequestDTO;
import com.esproject4.arenaufcg.DTO.PerfilPermissaoResponseDTO;
import com.esproject4.arenaufcg.DTO.PermissoesUsuarioResponseDTO;
import com.esproject4.arenaufcg.DTO.RegistroLogCommand;
import com.esproject4.arenaufcg.DTO.UserResponseDTO;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.PerfilPermissao;
import com.esproject4.arenaufcg.Model.PermissaoSistema;
import com.esproject4.arenaufcg.Model.UserRole;
import com.esproject4.arenaufcg.Repository.PerfilPermissaoRepository;

@Service
public class ControleAcessoService {

    private final PerfilPermissaoRepository repository;
    private final AuthService authService;
    private final AuditoriaService auditoriaService;

    public ControleAcessoService(
            PerfilPermissaoRepository repository,
            AuthService authService,
            AuditoriaService auditoriaService) {

        this.repository = repository;
        this.authService = authService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<PerfilPermissaoResponseDTO> listarPerfisEPermissoes(
            String authorization) {

        exigirAdmin(authorization);

        return repository.findAll()
            .stream()
            .map(PerfilPermissaoResponseDTO::de)
            .toList();
    }

    @Transactional
    public PerfilPermissaoResponseDTO atualizarPermissoesPerfil(
            UserRole perfil,
            AtualizarPermissoesRequestDTO dto,
            String authorization,
            String ipOrigem) {

        UserResponseDTO admin = exigirAdmin(authorization);

        PerfilPermissao perfilPermissao = buscarPerfil(perfil);

        Set<PermissaoSistema> novasPermissoes =
            converterPermissoes(dto.permissoes());

        perfilPermissao.atualizarPermissoes(
            novasPermissoes,
            admin.id()
        );

        registrarAlteracao(admin, perfil, ipOrigem);

        return PerfilPermissaoResponseDTO.de(perfilPermissao);
    }

    @Transactional(readOnly = true)
    public void verificarPermissao(
            UserRole perfil,
            PermissaoSistema permissao) {

        boolean possuiPermissao = perfil != null
            && permissao != null
            && repository.findById(perfil)
                .map(configuracao -> configuracao.possuiPermissao(permissao))
                .orElse(false);

        if (!possuiPermissao) {
            throw new RegraNegocioException(
                HttpStatus.FORBIDDEN,
                "ACESSO_NEGADO",
                "O perfil não possui a permissão necessária."
            );
        }
    }

    @Transactional(readOnly = true)
    public PermissoesUsuarioResponseDTO obterPermissoesDoUsuarioAutenticado(
            String authorization) {

        UserResponseDTO usuario =
            authService.getSessionUser(authorization);

        PerfilPermissao perfil = buscarPerfil(usuario.perfil());

        return new PermissoesUsuarioResponseDTO(
            perfil.getPerfil(),
            perfil.getPermissoes()
        );
    }

    // Métodos auxiliares

    private PerfilPermissao buscarPerfil(UserRole perfil) {
        if (perfil == null) {
            throw permissaoInvalida("Perfil inválido.");
        }

        return repository.findById(perfil)
            .orElseThrow(() ->
                permissaoInvalida("Configuração do perfil não encontrada.")
            );
    }

    private UserResponseDTO exigirAdmin(String authorization) {
        UserResponseDTO usuario =
            authService.getSessionUser(authorization);

        if (usuario.perfil() != UserRole.ADMIN) {
            throw new RegraNegocioException(
                HttpStatus.FORBIDDEN,
                "ACESSO_NEGADO",
                "Esta operação é exclusiva de administradores."
            );
        }

        return usuario;
    }

    private Set<PermissaoSistema> converterPermissoes(
            Set<String> permissoes) {

        if (permissoes == null) {
            throw permissaoInvalida(
                "Informe o conjunto de permissões."
            );
        }

        Set<PermissaoSistema> resultado =
            EnumSet.noneOf(PermissaoSistema.class);

        for (String permissao : permissoes) {
            if (permissao == null || permissao.isBlank()) {
                throw permissaoInvalida("Permissão inválida.");
            }

            try {
                resultado.add(
                    PermissaoSistema.valueOf(permissao)
                );
            } catch (IllegalArgumentException exception) {
                throw permissaoInvalida(
                    "Permissão não reconhecida: " + permissao
                );
            }
        }

        return resultado;
    }

    private RegraNegocioException permissaoInvalida(String mensagem) {
        return new RegraNegocioException(
            HttpStatus.BAD_REQUEST,
            "PERMISSAO_INVALIDA",
            mensagem
        );
    }

    private void registrarAlteracao(
            UserResponseDTO admin,
            UserRole perfil,
            String ipOrigem) {

        RegistroLogCommand comando = new RegistroLogCommand(
            admin.id(),
            admin.email(),
            admin.perfil(),
            "PERMISSOES_ATUALIZADAS",
            "/api/v1/admin/access-control/roles/" + perfil.name(),
            "PUT",
            200,
            "Permissões atualizadas para o perfil " + perfil.name(),
            ipOrigem
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