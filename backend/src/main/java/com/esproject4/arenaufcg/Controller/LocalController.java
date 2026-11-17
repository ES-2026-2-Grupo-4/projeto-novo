package com.esproject4.arenaufcg.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esproject4.arenaufcg.DTO.LocalRequest;
import com.esproject4.arenaufcg.Model.Local;
import com.esproject4.arenaufcg.Service.LocalService;

@RestController
@RequestMapping("/api/v1/venues")
public class LocalController {

    private final LocalService localService;

    public LocalController(LocalService localService) {
        this.localService = localService;
    }

    @PostMapping
    public ResponseEntity<Local> cadastrar(
            @RequestBody LocalRequest request) {

        Local local = localService.cadastrar(
            request.nome(),
            request.endereco(),
            request.tipo()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(local);
    }

    @GetMapping
    public List<Local> listar() {
      return localService.listar();
    }
}
