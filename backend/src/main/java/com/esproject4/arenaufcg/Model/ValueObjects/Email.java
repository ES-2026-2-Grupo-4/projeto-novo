package com.esproject4.arenaufcg.Model.ValueObjects;

import java.util.Objects;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Email(
    @JsonValue
    @Column(name = "email", length = 180, nullable = false, unique = true)
    String address
) {
    private static final int MAX_LENGTH = 180;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    @JsonCreator
    public Email {
        if (Objects.isNull(address) || address.isBlank()) {
            throw new IllegalArgumentException("O e-mail não pode ser nulo ou vazio.");
        }

        address = address.trim().toLowerCase();

        if (address.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "O e-mail excede o limite máximo de " + MAX_LENGTH + " caracteres."
            );
        }

        if (!EMAIL_PATTERN.matcher(address).matches()) {
            throw new IllegalArgumentException("Formato de e-mail inválido: " + address);
        }
    }

    public static Email of(String address) {
        return new Email(address);
    }

    @Override
    public String toString() {
        return address;
    }
}