package com.esproject4.arenaufcg.Controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import com.esproject4.arenaufcg.Model.Escalacao;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Service.FormacaoTaticaService;
import com.esproject4.arenaufcg.DTO.FormacaoRequestDTO;
import com.esproject4.arenaufcg.DTO.FormacoesResponseDTO;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FormacaoTaticaController {

    private final FormacaoTaticaService service;
    private final UsuarioAutenticado usuario;

    @PutMapping("/matches/{matchId}/lineups/{teamId}/formation")
    public EscalacaoResponseDTO definirFormacao(@PathVariable UUID matchId,
                                                @PathVariable UUID teamId,
                                                @Valid @RequestBody FormacaoRequestDTO dto) {
        Escalacao e = service.definirFormacao(matchId, teamId, dto.formacao(), usuario);
        return EscalacaoResponseDTO.from(e); // ADAPTAR: use o DTO de resposta que a spec do PUT define
    }

    @GetMapping("/modalities/{modality}/formations")
    public FormacoesResponseDTO listarFormacoes(@PathVariable Modalidade modality) {
        return service.listarFormacoes(modality);
    }
}
