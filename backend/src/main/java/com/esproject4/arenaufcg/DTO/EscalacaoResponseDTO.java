package com.esproject4.arenaufcg.DTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.esproject4.arenaufcg.Model.Atleta;
import com.esproject4.arenaufcg.Model.AtletaEscalado;
import com.esproject4.arenaufcg.Model.Escalacao;

// Dependência com T06 -- Atletas
public record EscalacaoResponseDTO(
        UUID id,
        UUID partidaId,
        UUID timeId,
        String formacao,
        long titulares,
        int relacionados,
        List<AtletaEscaladoDTO> atletas) {

    public static EscalacaoResponseDTO de(Escalacao e, Map<UUID, Atleta> atletasPorId) {
    List<AtletaEscaladoDTO> atletas = e.getAtletas().stream()
            .map(a -> paraDTO(a, atletasPorId))
            .toList();

    return new EscalacaoResponseDTO(
            e.getId(),
            e.getPartida().getId(),
            e.getTime().getId(),
            e.getFormacao(),
            e.contarTitulares(),
            e.contarRelacionados(),
            atletas);
    }

    private static AtletaEscaladoDTO paraDTO(AtletaEscalado a, Map<UUID, Atleta> atletasPorId) {
        Atleta atleta = atletasPorId.get(a.getAtletaId());
        String nome = (atleta != null) ? atleta.getNome() : null;
        LocalDate dataNascimento = (atleta != null) ? atleta.getDataNascimento() : null;
        return AtletaEscaladoDTO.de(a, nome, dataNascimento);
    }
}

