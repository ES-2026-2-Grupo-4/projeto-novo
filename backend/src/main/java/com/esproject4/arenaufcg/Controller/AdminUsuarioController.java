package com.esproject4.arenaufcg.Controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.esproject4.arenaufcg.DTO.AdminUsuarioResponseDTO;
import com.esproject4.arenaufcg.DTO.common.PaginaResponseDTO;
import com.esproject4.arenaufcg.Model.PermissaoSistema;
import com.esproject4.arenaufcg.Security.RequerPermissao;
import com.esproject4.arenaufcg.Service.AdminUsuarioService;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;

    public AdminUsuarioController(
            AdminUsuarioService adminUsuarioService) {
        this.adminUsuarioService = adminUsuarioService;
    }

    @RequerPermissao(
        value = {
            PermissaoSistema.AREA_ADMIN_ACESSO,
            PermissaoSistema.GERENCIAR_ADMINS
        },
        somenteAdmin = true
    )
    @GetMapping
    public ResponseEntity<PaginaResponseDTO<AdminUsuarioResponseDTO>> listar(
            @RequestHeader(value = "Authorization", required = false)
            String authorization,
            @PageableDefault(size = 20) Pageable pageable) {

        var administradores = adminUsuarioService.listarAdministradores(
            authorization,
            pageable
        );

        return ResponseEntity.ok(
            PaginaResponseDTO.de(administradores)
        );
    }
}