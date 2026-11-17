package com.esproject4.arenaufcg.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.esproject4.arenaufcg.Service.RegraNegocioException;

@RestControllerAdvice
public class ApiExceptionHandler {

    public record ErroResponse(String codigo, String mensagem) {}

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(
            RegraNegocioException exception) {

        ErroResponse erro = new ErroResponse(
            exception.getCodigo(),
            exception.getMessage()
        );

        return ResponseEntity
            .status(exception.getStatus())
            .body(erro);
    }
}
