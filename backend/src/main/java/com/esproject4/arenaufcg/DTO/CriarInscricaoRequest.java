package com.esproject4.arenaufcg.DTO;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CriarInscricaoRequest (
    @NotNull(message = "Informe o ID do time.")
        UUID timeId
) {}
