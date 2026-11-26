package com.esproject4.arenaufcg.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Na edição só nome e sigla mudam; a modalidade é fixa (RN-25).
public record EditarTimeRequest(

        @NotBlank(message = "Informe o nome do time.")
        @Size(max = 80, message = "O nome pode ter no máximo 80 caracteres.")
        String nome,

        @NotBlank(message = "Informe a sigla do time.")
        @Size(max = 5, message = "A sigla pode ter no máximo 5 caracteres.")
        String sigla
) {
}
