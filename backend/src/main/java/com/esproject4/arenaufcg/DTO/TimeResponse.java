package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Model.Time;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TimeResponse(
        UUID id,
        String nome,
        String sigla,
        Modalidade modalidade,
        UUID dirigenteId,
        OffsetDateTime criadoEm
) {

    public static TimeResponse from(Time time) {
        return new TimeResponse(
                time.getId(),
                time.getNome(),
                time.getSigla(),
                time.getModalidade(),
                time.getDirigenteId(),
                time.getCreatedAt()
        );
    }
}
