package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.Modalidade;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record CriarTimeRequest(

      @NotBlank
    String nome,

    @NotBlank
    String sigla,

    @NotNull
    Modalidade modalidade

) { }
