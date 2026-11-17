package com.esproject4.arenaufcg.DTO;

import java.time.LocalDate;

import com.esproject4.arenaufcg.Model.FormatoDisputa;
import com.esproject4.arenaufcg.Model.Modalidade;

public record CampeonatoRequest(
    String nome,
    String descricao,
    Modalidade modalidade,
    FormatoDisputa formato,
    Integer quantidadeGrupos,
    int maxTimes,
    LocalDate dataInicio,
    LocalDate dataFim
) {}
