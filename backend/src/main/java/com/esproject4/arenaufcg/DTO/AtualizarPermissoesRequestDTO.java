package com.esproject4.arenaufcg.DTO;

import java.util.Set;

import jakarta.validation.constraints.NotNull;

public record AtualizarPermissoesRequestDTO(

    @NotNull(message = "Informe o conjunto de permissões.")
    Set<String> permissoes

) {}