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

    public CampeonatoController(CampeonatoService campeonatoService) {
        this.campeonatoService = campeonatoService;
    }

    @PostMapping
    public ResponseEntity<CampeonatoResponseDTO> criar(
            @Valid @RequestBody CampeonatoRequestDTO request,
            @RequestAttribute(name = UsuarioAutenticado.ATRIBUTO, required = false) UUID usuarioId) {

        var campeonato = campeonatoService.criar(
            UsuarioAutenticado.exigir(usuarioId),
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
            @RequestAttribute(name = UsuarioAutenticado.ATRIBUTO, required = false) UUID usuarioId) {

        return campeonatoService.listarMeus(UsuarioAutenticado.exigir(usuarioId))
            .stream()
            .map(CampeonatoResponseDTO::de)
            .toList();
    }

    @GetMapping("/{id}")
    public CampeonatoResponseDTO buscar(
            @PathVariable("id") UUID id,
            @RequestAttribute(name = UsuarioAutenticado.ATRIBUTO, required = false) UUID usuarioId) {

        return CampeonatoResponseDTO.de(
            campeonatoService.buscar(id, UsuarioAutenticado.exigir(usuarioId))
        );
    }

    @PutMapping("/{id}")
    public CampeonatoResponseDTO atualizar(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CampeonatoRequestDTO request,
            @RequestAttribute(name = UsuarioAutenticado.ATRIBUTO, required = false) UUID usuarioId) {

        return CampeonatoResponseDTO.de(campeonatoService.atualizar(
            id,
            UsuarioAutenticado.exigir(usuarioId),
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
            @RequestAttribute(name = UsuarioAutenticado.ATRIBUTO, required = false) UUID usuarioId) {

        campeonatoService.remover(id, UsuarioAutenticado.exigir(usuarioId));

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/open-registrations")
    public CampeonatoResponseDTO abrirInscricoes(
            @PathVariable("id") UUID id,
            @RequestAttribute(name = UsuarioAutenticado.ATRIBUTO, required = false) UUID usuarioId) {

        return CampeonatoResponseDTO.de(
            campeonatoService.abrirInscricoes(id, UsuarioAutenticado.exigir(usuarioId))
        );
    }

    @PostMapping("/{id}/close-registrations")
    public CampeonatoResponseDTO encerrarInscricoes(
            @PathVariable("id") UUID id,
            @RequestAttribute(name = UsuarioAutenticado.ATRIBUTO, required = false) UUID usuarioId) {

        return CampeonatoResponseDTO.de(
            campeonatoService.encerrarInscricoes(id, UsuarioAutenticado.exigir(usuarioId))
        );
    }

    @PutMapping("/{id}/venues")
    public CampeonatoResponseDTO definirLocais(
            @PathVariable("id") UUID id,
            @Valid @RequestBody LocaisRequestDTO request,
            @RequestAttribute(name = UsuarioAutenticado.ATRIBUTO, required = false) UUID usuarioId) {

        return CampeonatoResponseDTO.de(campeonatoService.definirLocais(
            id,
            UsuarioAutenticado.exigir(usuarioId),
            request.localIds()
        ));
    }
}
