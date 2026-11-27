package com.esproject4.arenaufcg.DTO;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.esproject4.arenaufcg.Model.User;
import com.esproject4.arenaufcg.Model.UserRole;

public record AdminUsuarioResponseDTO(
    UUID id,
    String nome,
    String email,
    UserRole perfil,
    Boolean verificado,
    Boolean ativo,
    OffsetDateTime criadoEm
) {

    public static AdminUsuarioResponseDTO de(User user) {
        return new AdminUsuarioResponseDTO(
            user.getId(),
            user.getName(),
            UserResponseDTO.from(user).email(),
            user.getRole(),
            user.getVerified(),
            user.getActive(),
            user.getCreatedAt()
        );
    }
}