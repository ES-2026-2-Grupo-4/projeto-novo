package com.esproject4.arenaufcg.Controller;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.esproject4.arenaufcg.DTO.common.ErroResponseDTO;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;

import lombok.extern.slf4j.Slf4j;

// Tradução centralizada de exceções em respostas HTTP padronizadas (ErroResponseDTO).
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Exceções de regra de negócio: cada uma já carrega o status HTTP e o código bem como a mensagem de erro.
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDTO> handleRegraNegocio(RegraNegocioException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ErroResponseDTO(ex.getCodigo(), ex.getMessage()));
    }

    // Falha de validação do corpo (@Valid): junta os erros de campo numa única mensagem.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> handleValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatarErroCampo)
                .collect(Collectors.joining("; "));
        if (mensagem.isBlank()) {
            mensagem = "Dados inválidos.";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponseDTO("DADOS_INVALIDOS", mensagem));
    }

    // Corpo ausente ou JSON malformado.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> handleJsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponseDTO("JSON_INVALIDO",
                        "Corpo da requisição ausente ou malformado."));
    }

    // Exceções próprias do framework (404, 405, etc.) são repassadas para o Spring tratar com o status correçto.
    // Exceções inesperadas do tipo 500 passam aqui, com uma mensagem genérica sem expor dados sensíveis.
	@ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> handleGenerico(Exception ex) throws Exception {
        if (ex instanceof ErrorResponse) {
            throw ex;
        }
        log.error("Erro não tratado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResponseDTO("ERRO_INTERNO",
                        "Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }

    private static String formatarErroCampo(FieldError erro) {
        return erro.getField() + ": " + erro.getDefaultMessage();
    }
}
