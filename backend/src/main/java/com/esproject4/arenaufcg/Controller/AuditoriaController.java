package com.esproject4.arenaufcg.Controller;

import java.time.Instant;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.esproject4.arenaufcg.DTO.LogAuditoriaResponseDTO;
import com.esproject4.arenaufcg.DTO.common.PaginaResponseDTO;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.UserRole;
import com.esproject4.arenaufcg.Service.AuditoriaService;
import com.esproject4.arenaufcg.Model.PermissaoSistema;
import com.esproject4.arenaufcg.Security.RequerPermissao;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;
    private final UsuarioAutenticado usuarioAutenticado;

    public AuditoriaController(
            AuditoriaService auditoriaService,
            UsuarioAutenticado usuarioAutenticado) {

        this.auditoriaService = auditoriaService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @RequerPermissao(
        value = {
            PermissaoSistema.AREA_ADMIN_ACESSO,
            PermissaoSistema.VISUALIZAR_LOGS
        },
        somenteAdmin = true
    )
    @GetMapping
    public ResponseEntity<PaginaResponseDTO<LogAuditoriaResponseDTO>> consultarLogs(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String perfil,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) Instant inicio,
            @RequestParam(required = false) Instant fim,
            @PageableDefault(size = 20) Pageable pageable) {

        usuarioAutenticado.exigirAdmin(authorization);

        UserRole perfilUsuario = converterPerfil(perfil);

        return ResponseEntity.ok(
            PaginaResponseDTO.de(
                auditoriaService.consultarLogs(
                    perfilUsuario, acao, inicio, fim, pageable
                )
            )
        );
    }

    private UserRole converterPerfil(String perfil) {
        if (perfil == null) {
            return null;
        }

        try {
            return UserRole.valueOf(perfil);
        } catch (IllegalArgumentException exception) {
            throw new RegraNegocioException(
                HttpStatus.BAD_REQUEST,
                "PARAMETRO_INVALIDO",
                "O perfil informado é inválido."
            );
        }
    }
}