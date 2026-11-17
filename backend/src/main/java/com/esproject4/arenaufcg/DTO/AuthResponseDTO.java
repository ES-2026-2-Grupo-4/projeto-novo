package com.esproject4.arenaufcg.dto;

public record AuthResponseDTO(
        String accessToken,
        UserResponseDTO usuario
) {

    public static AuthResponseDTO of(String accessToken, UserResponseDTO usuario) {
        return new AuthResponseDTO(accessToken, usuario);
    }
}