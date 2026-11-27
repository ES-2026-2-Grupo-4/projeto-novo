package com.esproject4.arenaufcg.Controller;

import com.esproject4.arenaufcg.DTO.AtletaRequest;
import com.esproject4.arenaufcg.DTO.AtletaResponse;
import com.esproject4.arenaufcg.Service.AtletaService;
import com.esproject4.arenaufcg.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AtletaController {

    private final AtletaService atletas;
    private final AuthService auth;

    public AtletaController(AtletaService atletas, AuthService auth) {
        this.atletas = atletas;
        this.auth = auth;
    }

    @PostMapping("/teams/{teamId}/athletes")
    @ResponseStatus(HttpStatus.CREATED)
    public AtletaResponse cadastrar(@PathVariable UUID teamId, @Valid @RequestBody AtletaRequest request,
                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return atletas.cadastrar(teamId, auth.getSessionUser(authorization).id(), request);
    }

    @GetMapping("/teams/{teamId}/athletes")
    public List<AtletaResponse> listarDoTime(@PathVariable UUID teamId,
                                             @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return atletas.listarDoTime(teamId, auth.getSessionUser(authorization).id());
    }

    @PutMapping("/athletes/{athleteId}")
    public AtletaResponse atualizar(@PathVariable UUID athleteId, @Valid @RequestBody AtletaRequest request,
                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return atletas.atualizar(athleteId, auth.getSessionUser(authorization).id(), request);
    }

    @DeleteMapping("/athletes/{athleteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID athleteId,
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        atletas.remover(athleteId, auth.getSessionUser(authorization).id());
    }
}
