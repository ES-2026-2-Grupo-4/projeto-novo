package com.esproject4.arenaufcg.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
    private final UsuarioAutenticado usuarioAutenticado;

    public LocalController(LocalService localService, UsuarioAutenticado usuarioAutenticado) {
        this.localService = localService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @PostMapping
    public ResponseEntity<LocalResponseDTO> cadastrar(
            @Valid @RequestBody LocalRequestDTO request,
            @RequestHeader(name = "Authorization", required = false) String authorization) {

        var local = localService.cadastrar(
            usuarioAutenticado.exigirOrganizadorVerificado(authorization),
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
            @RequestHeader(name = "Authorization", required = false) String authorization) {

        usuarioAutenticado.exigirOrganizadorVerificado(authorization);

        return localService.listar().stream().map(LocalResponseDTO::de).toList();
    }
}
