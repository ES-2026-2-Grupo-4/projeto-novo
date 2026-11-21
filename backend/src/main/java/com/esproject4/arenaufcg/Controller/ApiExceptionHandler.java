package com.esproject4.arenaufcg.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.esproject4.arenaufcg.Service.RegraNegocioException;

@RestControllerAdvice
public class ApiExceptionHandler {

    public record ErroResponse(String codigo, String mensagem) {}

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException exception) {
        return ResponseEntity
            .status(exception.getStatus())
            .body(new ErroResponse(exception.getCodigo(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException exception) {
        String mensagem = exception.getBindingResult().getFieldErrors().stream()
            .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
            .findFirst()
            .orElse("Dados inválidos.");

        return ResponseEntity
            .badRequest()
            .body(new ErroResponse("DADOS_INVALIDOS", mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoInvalido(HttpMessageNotReadableException exception) {
        return ResponseEntity
            .badRequest()
            .body(new ErroResponse("DADOS_INVALIDOS", "Corpo da requisição inválido."));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErroResponse> tratarConcorrencia(ObjectOptimisticLockingFailureException exception) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErroResponse("CONFLITO_CONCORRENCIA",
                "O recurso foi alterado por outra operação. Tente novamente."));
    }

    @ExceptionHandler(
    com.esproject4.arenaufcg.Exception.RegraNegocioException.class
)
public ResponseEntity<ErroResponse> tratarRegraAutenticacao(
        com.esproject4.arenaufcg.Exception.RegraNegocioException exception) {

    return ResponseEntity
        .status(exception.getStatus())
        .body(new ErroResponse(
            exception.getCodigo(),
            exception.getMessage()
        ));
}
}
