package com.esproject4.arenaufcg.DTO;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.esproject4.arenaufcg.Model.User;
import com.esproject4.arenaufcg.Model.UserRole;

// Um único DTO atende cadastro, login e /me: devolve o conjunto completo de campos.
// O password e os tokens da entidade nunca entram aqui.
public record UserResponseDTO(
        UUID id,
        String nome,
        String email,
        UserRole perfil,
        boolean verificado,
        OffsetDateTime criadoEm) {

    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail().address(),
                user.getRole(),
                Boolean.TRUE.equals(user.getVerified()),
                user.getCreatedAt());
    }
}
