package com.esproject4.arenaufcg.Service.security;

public interface TokenProvider {
    String gerarToken(User usuario);
    TokenClaims validarToken(String token);
}
