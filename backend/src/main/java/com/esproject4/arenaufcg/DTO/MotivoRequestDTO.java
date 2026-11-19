package com.esproject4.arenaufcg.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MotivoRequestDTO(
    @NotBlank(message = "Informe o motivo.")
    @Size(max = 500, message = "O motivo pode ter no máximo 500 caracteres.") 
    String motivo) 
{}
