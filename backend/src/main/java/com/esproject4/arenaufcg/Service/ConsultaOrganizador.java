package com.esproject4.arenaufcg.Service;

import java.util.UUID;

/**
 * Consulta o perfil do usuário autenticado. A implementação real depende da entidade User (T02).
 */
public interface ConsultaOrganizador {

    boolean isOrganizador(UUID usuarioId);

    boolean isContaVerificada(UUID usuarioId);
}
