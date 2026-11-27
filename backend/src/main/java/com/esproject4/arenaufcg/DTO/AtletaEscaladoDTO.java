package com.esproject4.arenaufcg.DTO;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

import com.esproject4.arenaufcg.Model.AtletaEscalado;

// Dados públicos do atleta na escalação: SEM CPF e SEM data de nascimento (só a idade)
public record AtletaEscaladoDTO(
        UUID atletaId,
        String nome,
        Integer idade,
        Integer numero,
        String posicaoNaPartida,
        boolean titular
) {

    // nome e dataNascimento vêm do Atleta (T06), buscado pelo service
    public static AtletaEscaladoDTO de(AtletaEscalado escalado, String nome, LocalDate dataNascimento) {
        return new AtletaEscaladoDTO(
                escalado.getAtletaId(),
                nome,
                calcularIdade(dataNascimento, LocalDate.now()),
                escalado.getNumero(),
                escalado.getPosicaoNaPartida(),
                escalado.isTitular()
        );
    }

    public static Integer calcularIdade(LocalDate dataNascimento, LocalDate hoje) {
        if (dataNascimento == null) {
            return null;
        }
        return Period.between(dataNascimento, hoje).getYears();
    }
}
