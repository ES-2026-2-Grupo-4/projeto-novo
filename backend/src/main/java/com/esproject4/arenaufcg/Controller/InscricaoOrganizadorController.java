package com.esproject4.arenaufcg.Controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.esproject4.arenaufcg.DTO.InscricaoResponseDTO;
import com.esproject4.arenaufcg.DTO.MotivoRequestDTO;
import com.esproject4.arenaufcg.Model.StatusInscricao;
import com.esproject4.arenaufcg.Service.InscricaoOrganizadorService;

import jakarta.validation.Valid;

@RestController
public class InscricaoOrganizadorController {

    private final InscricaoOrganizadorService service;

    public InscricaoOrganizadorController(InscricaoOrganizadorService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/campeonatos/{campeonatoId}/inscricoes")
    public ResponseEntity<Page<InscricaoResponseDTO>> listar(
            @PathVariable UUID campeonatoId,
            @RequestParam(required = false) StatusInscricao status,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader("X-Usuario-Id") UUID usuarioAtualId) {

        return ResponseEntity.ok(
            service.listar(campeonatoId, status, pageable, usuarioAtualId)
        );
    }

    @PostMapping("/api/v1/inscricoes/{id}/aprovacao")
    public ResponseEntity<InscricaoResponseDTO> aprovar(
            @PathVariable UUID id,
            @RequestHeader("X-Usuario-Id") UUID usuarioAtualId) {

        return ResponseEntity.ok(
            service.aprovar(id, usuarioAtualId)
        );
    }

    @PostMapping("/api/v1/inscricoes/{id}/recusa")
    public ResponseEntity<InscricaoResponseDTO> recusar(
            @PathVariable UUID id,
            @Valid @RequestBody MotivoRequestDTO request,
            @RequestHeader("X-Usuario-Id") UUID usuarioAtualId) {

        return ResponseEntity.ok(
            service.recusar(id, request.motivo(), usuarioAtualId)
        );
    }

    @PostMapping("/api/v1/inscricoes/{id}/remocao")
    public ResponseEntity<InscricaoResponseDTO> remover(
            @PathVariable UUID id,
            @Valid @RequestBody MotivoRequestDTO request,
            @RequestHeader("X-Usuario-Id") UUID usuarioAtualId) {

        return ResponseEntity.ok(
            service.remover(id, request.motivo(), usuarioAtualId)
        );
    }
}