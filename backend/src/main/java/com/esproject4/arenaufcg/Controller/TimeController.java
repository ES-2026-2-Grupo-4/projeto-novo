package com.esproject4.arenaufcg.Controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import com.esproject4.arenaufcg.Service.AuthService;
import com.esproject4.arenaufcg.Service.TimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.esproject4.arenaufcg.DTO.CriarTimeRequest;
import com.esproject4.arenaufcg.DTO.TimeResponse;
import com.esproject4.arenaufcg.DTO.UserResponseDTO;
import jakarta.validation.Valid;
import com.esproject4.arenaufcg.Model.UserRole;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import java.util.List;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/times")
@RequiredArgsConstructor
public class TimeController {
    
    private final TimeService timeService;
    private final AuthService authService;

    
    // cadastro de time
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeResponse criar(
            @Valid @RequestBody CriarTimeRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        UserResponseDTO usuario = authService.getSessionUser(authorization);

        if (usuario.perfil() != UserRole.DIRIGENTE) {
            throw new RegraNegocioException(
                    HttpStatus.FORBIDDEN,
                    "ACESSO_NEGADO",
                    "Apenas dirigentes podem cadastrar times."
            );
        }

        return timeService.criar(request, usuario.id());
    }

     // Listagem dos times
    @GetMapping
    public List<TimeResponse> meusTimes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        UserResponseDTO usuario = authService.getSessionUser(authorization);

        if (usuario.perfil() != UserRole.DIRIGENTE) {
            throw new RegraNegocioException(
                    HttpStatus.FORBIDDEN,
                    "ACESSO_NEGADO",
                    "Apenas dirigentes podem consultar seus times."
            );
        }

        return timeService.meusTimes(usuario.id());
    }
}
