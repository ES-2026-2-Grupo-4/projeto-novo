package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Model.Time;

public record TimeResponse (
    Long id,
    String nome,
    String sigla,
    Modalidade modalidade
) {

     public static TimeResponse from(Time time) {
        return new TimeResponse(
            time.getId(),
            time.getNome(),
            time.getSigla(),
            time.getModalidade()
        );
    }
}
