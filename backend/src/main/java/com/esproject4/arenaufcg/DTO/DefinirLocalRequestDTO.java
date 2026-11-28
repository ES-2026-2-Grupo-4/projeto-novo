package com.esproject4.arenaufcg.DTO;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

// Corpo do PUT /api/v1/matches/{matchId}/venue: o dirigente do mandante escolhe o local.
// Nome distinto do LocalRequestDTO (que serve ao cadastro de locais: nome/endereco/tipo).
public record DefinirLocalRequestDTO(
    @NotNull(message = "O local é obrigatório.") UUID localId
) {}
