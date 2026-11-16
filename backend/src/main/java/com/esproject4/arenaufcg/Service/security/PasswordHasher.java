package com.esproject4.arenaufcg.Service;

public interface PasswordHasher {
    String hash(String senhaPlana);
    boolean verificar(String senhaPlana, String senhaHash);
}
