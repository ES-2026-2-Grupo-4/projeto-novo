package com.esproject4.arenaufcg.Service;

public interface TokenProvider {
    String gerarToken(User usuario);
    TokenClaims validarToken(String token);
}
