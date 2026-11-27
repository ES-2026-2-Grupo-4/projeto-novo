package com.esproject4.arenaufcg.DTO.common;

import java.util.List;
import org.springframework.data.domain.Page;

public record PaginaResponseDTO<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {

    public static <T> PaginaResponseDTO<T> de(Page<T> pagina) {
        return new PaginaResponseDTO<>(
            pagina.getContent(),
            pagina.getNumber(),
            pagina.getSize(),
            pagina.getTotalElements(),
            pagina.getTotalPages()
        );
    }
}