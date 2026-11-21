package com.esproject4.arenaufcg.DTO;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record LocaisRequestDTO(
    @NotNull List<@NotNull UUID> localIds
) {}
