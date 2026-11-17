package com.esproject4.arenaufcg.Service.security;

import com.esproject4.arenaufcg.Model.User;

public interface TokenProvider {
    String gerarToken(User usuario);
    TokenClaims validarToken(String token);
}
