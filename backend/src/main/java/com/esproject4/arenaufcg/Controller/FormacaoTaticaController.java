package com.esproject4.arenaufcg.Controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esproject4.arenaufcg.DTO.EscalacaoResponseDTO;
import com.esproject4.arenaufcg.DTO.FormacaoRequestDTO;
import com.esproject4.arenaufcg.DTO.FormacoesResponseDTO;
import com.esproject4.arenaufcg.Model.Escalacao;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Service.FormacaoTaticaService;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FormacaoTaticaController {

    private final FormacaoTaticaService service;
    private final UsuarioAutenticado usuarioAutenticado;

    @PutMapping("/matches/{matchId}/lineups/{teamId}/formation")
    public EscalacaoResponseDTO definirFormacao(@PathVariable UUID matchId,
                                                @PathVariable UUID teamId,
                                                @Valid @RequestBody FormacaoRequestDTO dto,
                                                @RequestHeader("Authorization") String authorization) {
        UUID usuarioId = usuarioAutenticado.exigir(authorization);
        Escalacao escalacao = service.definirFormacao(matchId, teamId, dto.formacao(), usuarioId);
        return EscalacaoResponseDTO.de(escalacao);
    }

    @GetMapping("/modalities/{modality}/formations")
    public FormacoesResponseDTO listarFormacoes(@PathVariable Modalidade modality) {
        return service.listarFormacoes(modality);
    }
}