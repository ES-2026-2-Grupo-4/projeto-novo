package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.AtletaElenco;
import com.esproject4.arenaufcg.Model.Posicao;
import java.util.UUID;

public record ElencoResponse(UUID id, UUID inscricaoId, UUID campeonatoId, UUID atletaId,
                             int numero, Posicao posicao, boolean ativo) {
    public static ElencoResponse de(AtletaElenco entrada) {
        return new ElencoResponse(entrada.getId(), entrada.getInscricao().getId(), entrada.getCampeonatoId(),
                entrada.getAtleta().getId(), entrada.getNumero(), entrada.getPosicao(), entrada.isAtivo());
    }
}
