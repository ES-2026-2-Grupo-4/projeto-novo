package com.esproject4.arenaufcg.DTO.auth;

import java.util.Locale;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.") String email,

        // No login não se valida tamanho nem complexidade: isso vazaria a política de
        // senha
        // e bloquearia contas criadas sob regras antigas. Quem decide é o BCrypt.
        @NotBlank(message = "A senha é obrigatória.") String senha) {

    public LoginRequestDTO {
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return "LoginRequestDTO[email=" + email + "]";
    }
}