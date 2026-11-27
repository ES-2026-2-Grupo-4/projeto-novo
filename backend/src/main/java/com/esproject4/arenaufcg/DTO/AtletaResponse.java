package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.Atleta;
import com.esproject4.arenaufcg.Model.Posicao;

import java.time.LocalDate;
import java.util.UUID;

public record AtletaResponse(
        UUID id, UUID timeId, String nome, String apelido,
        LocalDate dataNascimento, Posicao posicaoPreferida, boolean ativo
) {
    public static AtletaResponse de(Atleta atleta) {
        return new AtletaResponse(atleta.getId(), atleta.getTimeId(), atleta.getNome(),
                atleta.getApelido(), atleta.getDataNascimento(), atleta.getPosicaoPreferida(),
                atleta.isAtivo());
    }
}
