package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.TipoLocal;

public record LocalRequest(
        String nome,
        String endereco,
        TipoLocal tipo
) {
}
