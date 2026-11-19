package com.esproject4.arenaufcg.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esproject4.arenaufcg.DTO.LocalRequestDTO;
import com.esproject4.arenaufcg.DTO.LocalResponseDTO;
import com.esproject4.arenaufcg.Service.LocalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/venues")
public class LocalController {

    private final LocalService localService;

    public LocalController(LocalService localService) {
        this.localService = localService;
    }

    @PostMapping
    public ResponseEntity<LocalResponseDTO> cadastrar(
            @Valid @RequestBody LocalRequestDTO request,
            @RequestAttribute(name = UsuarioAutenticado.ATRIBUTO, required = false) UUID usuarioId) {

        var local = localService.cadastrar(
            UsuarioAutenticado.exigir(usuarioId),
            request.nome(),
            request.endereco(),
            request.tipo()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(LocalResponseDTO.de(local));
    }

    @GetMapping
    public List<LocalResponseDTO> listar(
            @RequestAttribute(name = UsuarioAutenticado.ATRIBUTO, required = false) UUID usuarioId) {

        UsuarioAutenticado.exigir(usuarioId);

        return localService.listar().stream().map(LocalResponseDTO::de).toList();
    }
}
