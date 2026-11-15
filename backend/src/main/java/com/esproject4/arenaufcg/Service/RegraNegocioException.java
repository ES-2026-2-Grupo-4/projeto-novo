package com.esproject4.arenaufcg.Service;

import org.springframework.http.HttpStatus;

public class RegraNegocioException extends RuntimeException {
    private final HttpStatus status;
    private final String codigo;
    
    public RegraNegocioException(HttpStatus status, String codigo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public HttpStatus getStatus() {
        return status;
    }

        
}
