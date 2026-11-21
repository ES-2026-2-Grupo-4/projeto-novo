package com.esproject4.arenaufcg.Service;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * TEMPORÁRIA: informa que não há inscrições ativas enquanto a entidade Inscricao (T05)
 * não está integrada. Substituir por uma implementação que consulte o InscricaoRepository.
 */
@Component
public class ConsultaInscricoesTemporaria implements ConsultaInscricoes {

    @Override
    public boolean possuiInscricoesAtivas(UUID campeonatoId) {
        return false;
    }
}
