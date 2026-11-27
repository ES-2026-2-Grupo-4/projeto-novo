package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.Modalidade;
import java.util.List;

public record FormacoesResponseDTO(
        Modalidade modalidade, int titulares, int maxRelacionados, List<String> formacoes) {}
