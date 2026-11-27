package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.Posicao;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ElencoRequest(@NotNull UUID atletaId, @NotNull Integer numero, @NotNull Posicao posicao) {}
