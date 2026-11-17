package com.esproject4.arenaufcg.Service;

import java.util.UUID;

/**
 * Consulta as inscrições de um campeonato. A implementação real depende da entidade Inscricao (T05).
 */
public interface ConsultaInscricoes {

    /** Indica se o campeonato tem inscrições PENDENTE ou APROVADA. */
    boolean possuiInscricoesAtivas(UUID campeonatoId);
}
