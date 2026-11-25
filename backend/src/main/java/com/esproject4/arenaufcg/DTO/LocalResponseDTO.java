package com.esproject4.arenaufcg.DTO;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.esproject4.arenaufcg.Model.Local;
import com.esproject4.arenaufcg.Model.TipoLocal;

public record LocalResponseDTO(
    UUID id,
    String nome,
    String endereco,
    TipoLocal tipo,
    OffsetDateTime criadoEm
) {

    public static LocalResponseDTO de(Local local) {
        return new LocalResponseDTO(
            local.getId(),
            local.getNome(),
            local.getEndereco(),
            local.getTipo(),
            local.getCriadoEm()
        );
    }
}
