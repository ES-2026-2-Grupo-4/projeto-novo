package com.esproject4.arenaufcg.DTO.auth;

import java.util.Locale;

import com.esproject4.arenaufcg.Model.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(

        @NotBlank(message = "O nome é obrigatório.") @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.") String nome,

        // O @Pattern repete a regex do value object Email: assim um e-mail como "a@b"
        // (que o @Email aceita) vira 400 aqui, em vez de IllegalArgumentException no
        // service.
        @NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.") @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "E-mail inválido.") @Size(max = 180, message = "O e-mail deve ter no máximo 180 caracteres.") String email,

        // max = 72: o BCrypt só considera os primeiros 72 bytes da senha.
        // A regra de complexidade (letra + número) é uma sugestão: o backlog não a
        // define.
        @NotBlank(message = "A senha é obrigatória.") @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.") @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "A senha deve conter letras e números.") String senha,

        @NotBlank(message = "A confirmação de senha é obrigatória.") String confirmaSenha,

        @NotNull(message = "O perfil é obrigatório.") UserRole perfil,

        // Boolean (e não boolean): @AssertTrue aceita null, então @NotNull cobre o
        // campo ausente.
        @NotNull(message = "O aceite dos termos é obrigatório.") @AssertTrue(message = "É necessário aceitar os Termos de Uso e a Política de Privacidade.") Boolean aceiteTermos) {

    // Construtor compacto: roda na desserialização, antes da validação.
    public RegisterRequestDTO {
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    // Valida senha == confirmaSenha. Se algum for null, deixa o @NotBlank reportar.
    @JsonIgnore
    @AssertTrue(message = "As senhas não conferem.")
    public boolean isSenhasConferem() {
        return senha == null || confirmaSenha == null || senha.equals(confirmaSenha);
    }

    // O toString padrão do record imprimiria a senha nos logs.
    @Override
    public String toString() {
        return "RegisterRequestDTO[nome=" + nome + ", email=" + email + ", perfil=" + perfil + "]";
    }
}