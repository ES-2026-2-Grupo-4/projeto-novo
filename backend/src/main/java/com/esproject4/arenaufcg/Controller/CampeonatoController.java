package com.esproject4.arenaufcg.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.esproject4.arenaufcg.DTO.CampeonatoRequestDTO;
import com.esproject4.arenaufcg.DTO.CampeonatoResponseDTO;
import com.esproject4.arenaufcg.DTO.LocaisRequestDTO;
import com.esproject4.arenaufcg.Service.CampeonatoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/championships")
public class CampeonatoController {

    private final CampeonatoService campeonatoService;
    private final UsuarioAutenticado usuarioAutenticado;

    public CampeonatoController(CampeonatoService campeonatoService, UsuarioAutenticado usuarioAutenticado) {
        this.campeonatoService = campeonatoService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @PostMapping
    public ResponseEntity<CampeonatoResponseDTO> criar(
            @Valid @RequestBody CampeonatoRequestDTO request,
            @RequestHeader(name = "Authorization", required = false) String authorization) {

        var campeonato = campeonatoService.criar(
            usuarioAutenticado.exigirOrganizadorVerificado(authorization),
            request.nome(),
            request.descricao(),
            request.modalidade(),
            request.formato(),
            request.quantidadeGrupos(),
            request.maxTimes(),
            request.dataInicio(),
            request.dataFim()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(CampeonatoResponseDTO.de(campeonato));
    }

    @GetMapping("/me")
    public List<CampeonatoResponseDTO> listarMeus(
          @RequestHeader(name = "Authorization", required = false) String authorization) {

        return campeonatoService.listarMeus(usuarioAutenticado.exigir(authorization))
            .stream()
            .map(CampeonatoResponseDTO::de)
            .toList();
    }

    @GetMapping("/{id}")
    public CampeonatoResponseDTO buscar(
            @PathVariable("id") UUID id,
            @RequestHeader(name = "Authorization", required = false) String authorization) {

        return CampeonatoResponseDTO.de(
            campeonatoService.buscar(id, usuarioAutenticado.exigir(authorization))
        );
    }

    @PutMapping("/{id}")
    public CampeonatoResponseDTO atualizar(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CampeonatoRequestDTO request,
            @RequestHeader(name = "Authorization", required = false) String authorization) {

        return CampeonatoResponseDTO.de(campeonatoService.atualizar(
            id,
            usuarioAutenticado.exigir(authorization),
            request.nome(),
            request.descricao(),
            request.modalidade(),
            request.formato(),
            request.quantidadeGrupos(),
            request.maxTimes(),
            request.dataInicio(),
            request.dataFim()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @PathVariable("id") UUID id,
            @RequestHeader(name = "Authorization", required = false) String authorization) {

        campeonatoService.remover(id, usuarioAutenticado.exigir(authorization));

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/open-registrations")
    public CampeonatoResponseDTO abrirInscricoes(
            @PathVariable("id") UUID id,
            @RequestHeader(name = "Authorization", required = false) String authorization) {

        return CampeonatoResponseDTO.de(
            campeonatoService.abrirInscricoes(id, usuarioAutenticado.exigir(authorization))
        );
    }

    @PostMapping("/{id}/close-registrations")
    public CampeonatoResponseDTO encerrarInscricoes(
            @PathVariable("id") UUID id,
            @RequestHeader(name = "Authorization", required = false) String authorization) {

        return CampeonatoResponseDTO.de(
            campeonatoService.encerrarInscricoes(id, usuarioAutenticado.exigir(authorization))
        );
    }

    @PutMapping("/{id}/venues")
    public CampeonatoResponseDTO definirLocais(
            @PathVariable("id") UUID id,
            @Valid @RequestBody LocaisRequestDTO request,
            @RequestHeader(name = "Authorization", required = false) String authorization) {

        return CampeonatoResponseDTO.de(campeonatoService.definirLocais(
            id,
            usuarioAutenticado.exigir(authorization),
            request.localIds()
        ));
    }
}
