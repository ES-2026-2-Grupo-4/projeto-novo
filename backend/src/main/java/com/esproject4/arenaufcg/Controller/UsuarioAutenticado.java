package com.esproject4.arenaufcg.Controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Service.RegraNegocioException;

/**
 * Lê o id do usuário que o filtro de autenticação (T02) coloca no atributo "usuarioId" da requisição.
 */
final class UsuarioAutenticado {

    static final String ATRIBUTO = "usuarioId";

    private UsuarioAutenticado() {}

    static UUID exigir(UUID usuarioId) {
        if (usuarioId == null) {
            throw new RegraNegocioException(
                HttpStatus.UNAUTHORIZED,
                "NAO_AUTENTICADO",
                "É necessário autenticar o usuário."
            );
        }

        return usuarioId;
    }
}
