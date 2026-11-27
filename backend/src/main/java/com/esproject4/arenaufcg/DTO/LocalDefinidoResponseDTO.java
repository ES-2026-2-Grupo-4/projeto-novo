package com.esproject4.arenaufcg.DTO;

import java.util.UUID;

import com.esproject4.arenaufcg.Model.Partida;

// Resposta do PUT /api/v1/matches/{matchId}/venue: confirma o local de mando definido.
public record LocalDefinidoResponseDTO(
        UUID partidaId,
        UUID localId,
        String status) {

    public static LocalDefinidoResponseDTO de(Partida partida) {
        return new LocalDefinidoResponseDTO(
                partida.getId(),
                partida.getLocalId(),
                partida.getStatus().name());
    }
}
