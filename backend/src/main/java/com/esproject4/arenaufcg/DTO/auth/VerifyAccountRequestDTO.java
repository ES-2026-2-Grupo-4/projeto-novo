package com.esproject4.arenaufcg.DTO.auth;

import java.util.Locale;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyAccountRequestDTO(

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        String email,

        @NotBlank(message = "O código de verificação é obrigatório.")
        @Pattern(regexp = "^\\d{6}$", message = "O código deve ter 6 dígitos.")
        String codigoVerificacao
) {

    public VerifyAccountRequestDTO {
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}