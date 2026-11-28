package com.esproject4.arenaufcg.DTO;

import jakarta.validation.constraints.NotBlank;

public record FormacaoRequestDTO(@NotBlank String formacao) {}

