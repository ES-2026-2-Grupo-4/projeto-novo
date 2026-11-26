package com.esproject4.arenaufcg.Controller;

import com.esproject4.arenaufcg.DTO.MembroStaffRequest;
import com.esproject4.arenaufcg.DTO.MembroStaffResponse;
import com.esproject4.arenaufcg.Service.MembroStaffService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class MembroStaffController {

    private final MembroStaffService staff;
    private final UsuarioAutenticado usuario;

    public MembroStaffController(MembroStaffService staff, UsuarioAutenticado usuario) {
        this.staff = staff;
        this.usuario = usuario;
    }

    @PostMapping("/teams/{teamId}/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public MembroStaffResponse cadastrar(@PathVariable UUID teamId, @Valid @RequestBody MembroStaffRequest request,
                                         @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return staff.cadastrar(teamId, usuario.exigir(authorization), request);
    }

    @GetMapping("/teams/{teamId}/staff")
    public List<MembroStaffResponse> listarDoTime(@PathVariable UUID teamId,
                                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return staff.listarDoTime(teamId, usuario.exigir(authorization));
    }

    @PutMapping("/staff/{staffId}")
    public MembroStaffResponse atualizar(@PathVariable UUID staffId, @Valid @RequestBody MembroStaffRequest request,
                                         @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return staff.atualizar(staffId, usuario.exigir(authorization), request);
    }

    @DeleteMapping("/staff/{staffId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID staffId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        staff.remover(staffId, usuario.exigir(authorization));
    }
}
