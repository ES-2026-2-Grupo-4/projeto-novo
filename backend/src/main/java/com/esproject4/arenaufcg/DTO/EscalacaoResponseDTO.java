package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.AtletaEscalado;
import com.esproject4.arenaufcg.Model.Escalacao;

import java.util.List;
import java.util.UUID;

// Dependência com T06 -- Atletas
public record EscalacaoResponseDTO(
        UUID id,
        UUID partidaId,
        UUID timeId,
        String formacao,
        long titulares,
        int relacionados,
        List<AtletaEscaladoResponseDTO> atletas) {

    public static EscalacaoResponseDTO de(Escalacao e) {
        return new EscalacaoResponseDTO(
                e.getId(),
                e.getPartida().getId(),
                e.getTime().getId(),
                e.getFormacao(),
                e.contarTitulares(),
                e.contarRelacionados(),
                e.getAtletas().stream().map(AtletaEscaladoResponseDTO::from).toList());


    }
}

