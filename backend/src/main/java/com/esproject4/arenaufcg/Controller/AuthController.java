package com.esproject4.arenaufcg.Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.esproject4.arenaufcg.DTO.auth.AuthResponseDTO;
import com.esproject4.arenaufcg.DTO.auth.ForgotPasswordRequestDTO;
import com.esproject4.arenaufcg.DTO.auth.LoginRequestDTO;
import com.esproject4.arenaufcg.DTO.auth.RegisterRequestDTO;
import com.esproject4.arenaufcg.DTO.auth.ResetPasswordRequestDTO;
import com.esproject4.arenaufcg.DTO.auth.UsuarioResponseDTO;
import com.esproject4.arenaufcg.DTO.auth.VerifyAccountRequestDTO;
import com.esproject4.arenaufcg.DTO.auth.VerifyAccountResponseDTO;
import com.esproject4.arenaufcg.DTO.common.MensagemResponseDTO;
import com.esproject4.arenaufcg.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO dto) {
        return authService.register(dto);
    }


    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return authService.login(dto);
    }

    // POST /api/v1/auth/verify -> 200 | 400
    @PostMapping("/verify")
    public VerifyAccountResponseDTO verify(@Valid @RequestBody VerifyAccountRequestDTO dto) {
        authService.verifyAccount(dto.codigoVerificacao(), dto.email());
        return new VerifyAccountResponseDTO("Conta verificada com sucesso.", true);
    }

    @GetMapping("/me")
    public UsuarioResponseDTO me(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return authService.getSessionUser(authorization);
    }

    @PostMapping("/forgot-password")
    public MensagemResponseDTO forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO dto) {
        authService.forgotPassword(dto.email());
        return new MensagemResponseDTO(
                "Se o e-mail estiver cadastrado, enviaremos as instruções de recuperação.");
    }

    @PostMapping("/reset-password/{token}")
    public MensagemResponseDTO resetPassword(
            @PathVariable String token,
            @Valid @RequestBody ResetPasswordRequestDTO dto) {
        authService.resetPassword(dto.senha(), dto.confirmaSenha(), token);
        return new MensagemResponseDTO("Senha redefinida com sucesso.");
    }
}