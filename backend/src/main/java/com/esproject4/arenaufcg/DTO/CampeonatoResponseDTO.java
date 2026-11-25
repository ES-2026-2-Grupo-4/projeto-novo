package com.esproject4.arenaufcg.DTO;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.esproject4.arenaufcg.Model.Campeonato;
import com.esproject4.arenaufcg.Model.FormatoDisputa;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Model.StatusCampeonato;

public record CampeonatoResponseDTO(
    UUID id,
    String nome,
    String descricao,
    Modalidade modalidade,
    FormatoDisputa formato,
    Integer quantidadeGrupos,
    int maxTimes,
    int vagasOcupadas,
    StatusCampeonato status,
    LocalDate dataInicio,
    LocalDate dataFim,
    UUID organizadorId,
    List<LocalResponseDTO> locais,
    OffsetDateTime criadoEm
) {

    public static CampeonatoResponseDTO de(Campeonato campeonato) {
        return new CampeonatoResponseDTO(
            campeonato.getId(),
            campeonato.getNome(),
            campeonato.getDescricao(),
            campeonato.getModalidade(),
            campeonato.getFormato(),
            campeonato.getQuantidadeGrupos(),
            campeonato.getMaxTimes(),
            campeonato.getVagasOcupadas(),
            campeonato.getStatus(),
            campeonato.getDataInicio(),
            campeonato.getDataFim(),
            campeonato.getOrganizadorId(),
            campeonato.getLocais().stream().map(LocalResponseDTO::de).toList(),
            campeonato.getCriadoEm()
        );
    }
}
