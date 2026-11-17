package com.esproject4.arenaufcg.Service.security;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class RegraNegocioException extends RuntimeException {
    private final HttpStatus status;
    private final String codigo;

    public RegraNegocioException(HttpStatus status, String codigo, String mensagem){
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
    }
}
