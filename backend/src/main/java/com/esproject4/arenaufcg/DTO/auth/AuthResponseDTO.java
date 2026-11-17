package com.esproject4.arenaufcg.DTO.auth;

import com.esproject4.arenaufcg.DTO.UserResponseDTO;

public record AuthResponseDTO(
        String accessToken,
        UserResponseDTO usuario) {

    public static AuthResponseDTO of(String accessToken, UserResponseDTO usuario) {
        return new AuthResponseDTO(accessToken, usuario);
    }
}