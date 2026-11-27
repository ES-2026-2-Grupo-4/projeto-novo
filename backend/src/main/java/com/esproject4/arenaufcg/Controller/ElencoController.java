package com.esproject4.arenaufcg.Controller;

import com.esproject4.arenaufcg.DTO.AtualizarElencoRequest;
import com.esproject4.arenaufcg.DTO.ElencoRequest;
import com.esproject4.arenaufcg.DTO.ElencoResponse;
import com.esproject4.arenaufcg.Service.ElencoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ElencoController {

    private final ElencoService elenco;
    private final UsuarioAutenticado usuario;

    public ElencoController(ElencoService elenco, UsuarioAutenticado usuario) {
        this.elenco = elenco;
        this.usuario = usuario;
    }

    @PostMapping("/registrations/{registrationId}/roster")
    @ResponseStatus(HttpStatus.CREATED)
    public ElencoResponse adicionar(@PathVariable UUID registrationId, @Valid @RequestBody ElencoRequest request,
                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return elenco.adicionar(registrationId, usuario.exigir(authorization), request);
    }

    @GetMapping("/registrations/{registrationId}/roster")
    public List<ElencoResponse> listar(@PathVariable UUID registrationId,
                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return elenco.listar(registrationId, usuario.exigir(authorization));
    }

    @PutMapping("/roster/{entryId}")
    public ElencoResponse atualizar(@PathVariable UUID entryId, @Valid @RequestBody AtualizarElencoRequest request,
                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return elenco.atualizar(entryId, usuario.exigir(authorization), request);
    }

    @DeleteMapping("/roster/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID entryId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        elenco.remover(entryId, usuario.exigir(authorization));
    }
}
