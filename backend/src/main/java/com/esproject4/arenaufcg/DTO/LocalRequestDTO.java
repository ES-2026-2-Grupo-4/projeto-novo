package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.TipoLocal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LocalRequestDTO(
    @NotBlank @Size(max = 120) String nome,
    @Size(max = 255) String endereco,
    @NotNull TipoLocal tipo
) {}
