package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.Posicao;
import jakarta.validation.constraints.NotNull;

public record AtualizarElencoRequest(@NotNull Integer numero, @NotNull Posicao posicao) {}
