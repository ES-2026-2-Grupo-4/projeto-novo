package com.esproject4.arenaufcg.DTO;

import java.time.LocalDate;

import com.esproject4.arenaufcg.Model.FormatoDisputa;
import com.esproject4.arenaufcg.Model.Modalidade;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CampeonatoRequestDTO(
    @NotBlank @Size(max = 120) String nome,
    @Size(max = 1000) String descricao,
    @NotNull Modalidade modalidade,
    @NotNull FormatoDisputa formato,
    Integer quantidadeGrupos,
    @NotNull @Min(2) @Max(64) Integer maxTimes,
    @NotNull LocalDate dataInicio,
    @NotNull LocalDate dataFim
) {}
