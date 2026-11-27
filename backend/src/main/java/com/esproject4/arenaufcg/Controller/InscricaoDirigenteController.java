package com.esproject4.arenaufcg.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.esproject4.arenaufcg.DTO.CriarInscricaoRequest;
import com.esproject4.arenaufcg.DTO.InscricaoResponseDTO;
import com.esproject4.arenaufcg.DTO.UserResponseDTO;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.UserRole;
import com.esproject4.arenaufcg.Service.AuthService;
import com.esproject4.arenaufcg.Service.InscricaoDirigenteService;

import jakarta.validation.Valid;

@RestController
public class InscricaoDirigenteController {
    
    private final InscricaoDirigenteService service;
    private final AuthService authService;

    public InscricaoDirigenteController(InscricaoDirigenteService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @PostMapping("/api/v1/championships/{championshipId}/registrations")
    public ResponseEntity<InscricaoResponseDTO> solicitar(
            @PathVariable UUID championshipId,
            @Valid @RequestBody CriarInscricaoRequest request,
            @RequestHeader("Authorization") String authorization) {

        UUID dirigenteId = obterDirigenteId(authorization);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.solicitar(championshipId, request.timeId(), dirigenteId)
        );
    }

    @PostMapping("/api/v1/registrations/{registrationId}/cancel")
    public ResponseEntity<InscricaoResponseDTO> cancelar(
            @PathVariable UUID registrationId,
            @RequestHeader("Authorization") String authorization) {

        UUID dirigenteId = obterDirigenteId(authorization);

        return ResponseEntity.ok(
                service.cancelar(registrationId, dirigenteId)
        );
    }

    @GetMapping("/api/v1/teams/{teamId}/registrations")
    public ResponseEntity<List<InscricaoResponseDTO>> listar(
            @PathVariable UUID teamId,
            @RequestHeader("Authorization") String authorization) {

        UUID dirigenteId = obterDirigenteId(authorization);

        return ResponseEntity.ok(
                service.listar(teamId, dirigenteId)
        );
    }

    private UUID obterDirigenteId(String authorization) {

        UserResponseDTO usuario = authService.getSessionUser(authorization);

        if (usuario.perfil() != UserRole.DIRIGENTE) {
            throw new RegraNegocioException(
                    HttpStatus.FORBIDDEN,
                    "ACESSO_NEGADO",
                    "Somente dirigentes podem realizar esta operação."
            );
        }

        return usuario.id();
    }
}
