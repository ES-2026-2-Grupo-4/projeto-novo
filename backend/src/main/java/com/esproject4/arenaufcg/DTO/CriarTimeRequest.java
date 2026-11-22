package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.Modalidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarTimeRequest(

        @NotBlank(message = "Informe o nome do time.")
        @Size(max = 80, message = "O nome pode ter no máximo 80 caracteres.")
        String nome,

        @NotBlank(message = "Informe a sigla do time.")
        @Size(max = 5, message = "A sigla pode ter no máximo 5 caracteres.")
        String sigla,

        @NotNull(message = "Informe a modalidade.")
        Modalidade modalidade
) {
}
