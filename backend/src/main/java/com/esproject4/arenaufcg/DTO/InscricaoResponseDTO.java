package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.Inscricao;
import com.esproject4.arenaufcg.Model.StatusInscricao;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InscricaoResponseDTO(
        UUID id,
        CampeonatoResumo campeonato,
        TimeResumo time,
        StatusInscricao status,
        String motivo,
        OffsetDateTime solicitadaEm,
        OffsetDateTime decididaEm
) {

    public record CampeonatoResumo(UUID id, String nome) {}

    public record TimeResumo(UUID id, String nome, String sigla) {}

    public static InscricaoResponseDTO de(Inscricao inscricao) {
        return new InscricaoResponseDTO(
                inscricao.getId(),
                new CampeonatoResumo(
                        inscricao.getCampeonato().getId(),
                        inscricao.getCampeonato().getNome()),
                new TimeResumo(
                        inscricao.getTime().getId(),
                        inscricao.getTime().getNome(),
                        inscricao.getTime().getSigla()),
                inscricao.getStatus(),
                inscricao.getMotivo(),
                inscricao.getSolicitadaEm(),
                inscricao.getDecididaEm()
        );
    }
}