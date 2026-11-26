package com.esproject4.arenaufcg.Controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.esproject4.arenaufcg.DTO.UserResponseDTO;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.UserRole;
import com.esproject4.arenaufcg.Service.AuthService;

@Component
public class UsuarioAutenticado {

    private final AuthService authService;

    public UsuarioAutenticado(AuthService authService) {
        this.authService = authService;
    }

    public UUID exigir(String authorization) {
        return authService.getSessionUser(authorization).id();
    }

    public UUID exigirOrganizador(String authorization) {
        UserResponseDTO usuario = authService.getSessionUser(authorization);

        validarOrganizador(usuario);

        return usuario.id();
    }

    public UUID exigirOrganizadorVerificado(String authorization) {
        UserResponseDTO usuario = authService.getSessionUser(authorization);

        validarOrganizador(usuario);

        if (!usuario.verificado()) {
            throw new RegraNegocioException(
                HttpStatus.FORBIDDEN,
                "CONTA_NAO_VERIFICADA",
                "Verifique sua conta antes de criar um campeonato."
            );
        }

        return usuario.id();
    }

    private void validarOrganizador(UserResponseDTO usuario) {
        if (usuario.perfil() != UserRole.ORGANIZADOR) {
            throw new RegraNegocioException(
                HttpStatus.FORBIDDEN,
                "ACESSO_NEGADO",
                "Esta operação é exclusiva de organizadores."
            );
        }
    }

    public UUID exigirAdmin(String authorization) {
        UserResponseDTO usuario = authService.getSessionUser(authorization);

        if (usuario.perfil() != UserRole.ADMIN) {
            throw new RegraNegocioException(
                HttpStatus.FORBIDDEN,
                "ACESSO_NEGADO",
                "Esta operação é exclusiva de administradores."
            );
        }

        return usuario.id();
    }
}
