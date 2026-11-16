package com.esproject4.arenaufcg.Service.security;

public interface PasswordHasher {
    String hash(String senhaPlana);
    boolean verificar(String senhaPlana, String senhaHash);
}
