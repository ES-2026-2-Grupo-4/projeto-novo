package com.esproject4.arenaufcg.Controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esproject4.arenaufcg.DTO.DefinirLocalRequestDTO;
import com.esproject4.arenaufcg.DTO.LocalDefinidoResponseDTO;
import com.esproject4.arenaufcg.Model.Partida;
import com.esproject4.arenaufcg.Service.MandoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/matches")
public class MandoController {

    private final MandoService mandoService;
    private final UsuarioAutenticado usuarioAutenticado;

    public MandoController(MandoService mandoService, UsuarioAutenticado usuarioAutenticado) {
        this.mandoService = mandoService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @PutMapping("/{matchId}/venue")
    public LocalDefinidoResponseDTO definirLocal(
            @PathVariable("matchId") UUID matchId,
            @Valid @RequestBody DefinirLocalRequestDTO request,
            @RequestHeader(name = "Authorization", required = false) String authorization) {

        UUID usuarioId = usuarioAutenticado.exigir(authorization);
        Partida partida = mandoService.definirLocal(usuarioId, matchId, request.localId());
        return LocalDefinidoResponseDTO.de(partida);
    }
}
