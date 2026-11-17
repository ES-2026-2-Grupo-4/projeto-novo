package com.esproject4.arenaufcg.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.esproject4.arenaufcg.DTO.CampeonatoRequest;
import com.esproject4.arenaufcg.DTO.LocaisRequest;
import com.esproject4.arenaufcg.Model.Campeonato;
import com.esproject4.arenaufcg.Service.CampeonatoService;
import com.esproject4.arenaufcg.Service.RegraNegocioException;

@RestController
@RequestMapping("/api/v1/championships")
public class CampeonatoController {

    private final CampeonatoService campeonatoService;

    public CampeonatoController(CampeonatoService campeonatoService) {
        this.campeonatoService = campeonatoService;
    }

    @PostMapping
    public ResponseEntity<Campeonato> criar(
            @RequestBody CampeonatoRequest request,
            @RequestAttribute(
                name = "usuarioId", required = false
            ) UUID usuarioId) {

        Campeonato campeonato = campeonatoService.criar(
            request.nome(),
            request.descricao(),
            request.modalidade(),
            request.formato(),
            request.quantidadeGrupos(),
            request.maxTimes(),
            request.dataInicio(),
            request.dataFim(),
            exigirUsuario(usuarioId)
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(campeonato);
    }

    @GetMapping("/me")
    public List<Campeonato> listarMeus(
            @RequestAttribute(
                name = "usuarioId", required = false
            ) UUID usuarioId) {

        return campeonatoService.listarMeus(exigirUsuario(usuarioId));
    }

    @GetMapping("/{id}")
    public Campeonato buscar(
            @PathVariable("id") UUID id,
            @RequestAttribute(
                name = "usuarioId", required = false
            ) UUID usuarioId) {

        return campeonatoService.buscar(id, exigirUsuario(usuarioId));
    }

    @PutMapping("/{id}")
    public Campeonato atualizar(
            @PathVariable("id") UUID id,
            @RequestBody CampeonatoRequest request,
            @RequestAttribute(
                name = "usuarioId", required = false
            ) UUID usuarioId) {

        return campeonatoService.atualizar(
            id,
            exigirUsuario(usuarioId),
            request.nome(),
            request.descricao(),
            request.modalidade(),
            request.formato(),
            request.quantidadeGrupos(),
            request.maxTimes(),
            request.dataInicio(),
            request.dataFim()
        );
    }

    @PostMapping("/{id}/open-registrations")
    public Campeonato abrirInscricoes(
            @PathVariable("id") UUID id,
            @RequestAttribute(
                name = "usuarioId", required = false
            ) UUID usuarioId) {

        return campeonatoService.abrirInscricoes(
            id, exigirUsuario(usuarioId)
        );
    }

    @PostMapping("/{id}/close-registrations")
    public Campeonato encerrarInscricoes(
            @PathVariable("id") UUID id,
            @RequestAttribute(
                name = "usuarioId", required = false
            ) UUID usuarioId) {

        return campeonatoService.encerrarInscricoes(
            id, exigirUsuario(usuarioId)
        );
    }

    @PutMapping("/{id}/venues")
    public Campeonato definirLocais(
            @PathVariable("id") UUID id,
            @RequestBody LocaisRequest request,
            @RequestAttribute(
                name = "usuarioId", required = false
            ) UUID usuarioId) {

        return campeonatoService.definirLocais(
            id,
            exigirUsuario(usuarioId),
            request.localIds()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @PathVariable("id") UUID id,
            @RequestAttribute(
                name = "usuarioId", required = false
            ) UUID usuarioId) {

        campeonatoService.buscar(id, exigirUsuario(usuarioId));

        throw new RegraNegocioException(
            HttpStatus.NOT_IMPLEMENTED,
            "OPERACAO_NAO_IMPLEMENTADA",
            "A remoção depende da verificação de inscrições."
        );
    }

    private UUID exigirUsuario(UUID usuarioId) {
        if (usuarioId == null) {
            throw new RegraNegocioException(
                HttpStatus.UNAUTHORIZED,
                "NAO_AUTENTICADO",
                "É necessário autenticar o usuário."
            );
        }

        return usuarioId;
    }
}
