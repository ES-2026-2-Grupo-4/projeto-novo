package com.esproject4.arenaufcg.DTO.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// O token vem na URL (/reset-password/{token}), não no corpo.
public record ResetPasswordRequestDTO(

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                 message = "A senha deve conter letras e números.")
        String senha,

        @NotBlank(message = "A confirmação de senha é obrigatória.")
        String confirmaSenha
) {

    @JsonIgnore
    @AssertTrue(message = "As senhas não conferem.")
    public boolean senhasConferem() {
        return senha == null || confirmaSenha == null || senha.equals(confirmaSenha);
    }

    // Evita imprimir a senha nos logs.
    @Override
    public String toString() {
        return "ResetPasswordRequestDTO[***]";
    }
}