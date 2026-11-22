package com.esproject4.arenaufcg.Controller;

import com.esproject4.arenaufcg.DTO.CriarTimeRequest;
import com.esproject4.arenaufcg.DTO.EditarTimeRequest;
import com.esproject4.arenaufcg.DTO.TimeResponse;
import com.esproject4.arenaufcg.DTO.UserResponseDTO;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.UserRole;
import com.esproject4.arenaufcg.Service.AuthService;
import com.esproject4.arenaufcg.Service.TimeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
public class TimeController {

    private final TimeService timeService;
    private final AuthService authService;

    public TimeController(TimeService timeService, AuthService authService) {
        this.timeService = timeService;
        this.authService = authService;
    }

    // POST /api/v1/teams - cadastro de time (HU-01)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeResponse criar(
            @Valid @RequestBody CriarTimeRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        UserResponseDTO usuario = dirigenteAutenticado(authorization);
        return timeService.criar(request, usuario.id());
    }

    // GET /api/v1/teams/me - times do dirigente logado
    @GetMapping("/me")
    public List<TimeResponse> meusTimes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        UserResponseDTO usuario = dirigenteAutenticado(authorization);
        return timeService.meusTimes(usuario.id());
    }

    // PUT /api/v1/teams/{teamId} - edição de nome e sigla (HU-02)
    @PutMapping("/{teamId}")
    public TimeResponse editar(
            @PathVariable UUID teamId,
            @Valid @RequestBody EditarTimeRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        // Aqui não precisa checar o perfil: o service confere se o usuário é o dono do time.
        UserResponseDTO usuario = authService.getSessionUser(authorization);
        return timeService.editar(teamId, request, usuario.id());
    }

    // Confere o login e se o perfil é DIRIGENTE, num lugar só.
    private UserResponseDTO dirigenteAutenticado(String authorization) {
        UserResponseDTO usuario = authService.getSessionUser(authorization);

        if (usuario.perfil() != UserRole.DIRIGENTE) {
            throw new RegraNegocioException(
                    HttpStatus.FORBIDDEN,
                    "ACESSO_NEGADO",
                    "Apenas dirigentes podem gerenciar times."
            );
        }
        return usuario;
    }
}
