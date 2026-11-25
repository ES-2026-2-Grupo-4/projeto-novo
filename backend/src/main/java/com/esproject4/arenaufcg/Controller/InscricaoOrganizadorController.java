package com.esproject4.arenaufcg.Controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.esproject4.arenaufcg.DTO.InscricaoResponseDTO;
import com.esproject4.arenaufcg.DTO.MotivoRequestDTO;
import com.esproject4.arenaufcg.Model.StatusInscricao;
import com.esproject4.arenaufcg.Service.AuthService;
import com.esproject4.arenaufcg.Service.InscricaoOrganizadorService;

import jakarta.validation.Valid;

@RestController
public class InscricaoOrganizadorController {

    private final InscricaoOrganizadorService service;
    private final AuthService authService;

    public InscricaoOrganizadorController(InscricaoOrganizadorService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @GetMapping("/api/v1/championships/{championshipId}/registrations")
    public ResponseEntity<Page<InscricaoResponseDTO>> listar(
            @PathVariable UUID championshipId,
            @RequestParam(required = false) StatusInscricao status,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        return ResponseEntity.ok(
            service.listar(championshipId, status, pageable, usuarioId(authorization))
        );
    }

    @PostMapping("/api/v1/registrations/{registrationId}/approve")
    public ResponseEntity<InscricaoResponseDTO> aprovar(
            @PathVariable UUID registrationId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        return ResponseEntity.ok(service.aprovar(registrationId, usuarioId(authorization)));
    }

    @PostMapping("/api/v1/registrations/{registrationId}/reject")
    public ResponseEntity<InscricaoResponseDTO> recusar(
            @PathVariable UUID registrationId,
            @Valid @RequestBody MotivoRequestDTO request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        return ResponseEntity.ok(
            service.recusar(registrationId, request.motivo(), usuarioId(authorization))
        );
    }

    @PostMapping("/api/v1/registrations/{registrationId}/remove")
    public ResponseEntity<InscricaoResponseDTO> remover(
            @PathVariable UUID registrationId,
            @Valid @RequestBody MotivoRequestDTO request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        return ResponseEntity.ok(
            service.remover(registrationId, request.motivo(), usuarioId(authorization))
        );
    }

    // O service já confere se o usuário é o organizador do campeonato.
    private UUID usuarioId(String authorization) {
        return authService.getSessionUser(authorization).id();
    }
}