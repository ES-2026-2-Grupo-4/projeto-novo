package com.esproject4.arenaufcg.Service;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * TEMPORÁRIA: considera todo usuário um organizador verificado enquanto a entidade User (T02)
 * não está integrada. Substituir por uma implementação que consulte o UserRepository.
 */
@Component
public class ConsultaOrganizadorTemporaria implements ConsultaOrganizador {

    @Override
    public boolean isOrganizador(UUID usuarioId) {
        return true;
    }

    @Override
    public boolean isContaVerificada(UUID usuarioId) {
        return true;
    }
}
