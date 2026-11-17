package com.esproject4.arenaufcg.Service.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String senhaPlana) {
        return encoder.encode(senhaPlana);
    }

    @Override
    public boolean verificar(String senhaPlana, String senhaHash) {
        return senhaPlana != null && senhaHash != null && encoder.matches(senhaPlana, senhaHash);
    }
}
