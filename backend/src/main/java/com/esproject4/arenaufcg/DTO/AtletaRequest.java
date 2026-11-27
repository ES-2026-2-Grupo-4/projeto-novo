package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.Posicao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AtletaRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 60) String apelido,
        @Past LocalDate dataNascimento,
        Posicao posicaoPreferida
) {}
