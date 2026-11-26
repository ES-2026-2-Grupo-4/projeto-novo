package com.esproject4.arenaufcg.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.esproject4.arenaufcg.DTO.AtualizarPermissoesRequestDTO;
import com.esproject4.arenaufcg.DTO.PerfilPermissaoResponseDTO;
import com.esproject4.arenaufcg.DTO.PermissoesUsuarioResponseDTO;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.UserRole;
import com.esproject4.arenaufcg.Service.ControleAcessoService;
import com.esproject4.arenaufcg.Model.PermissaoSistema;
import com.esproject4.arenaufcg.Security.RequerPermissao;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class ControleAcessoController {

    private final ControleAcessoService controleAcessoService;

    public ControleAcessoController(
            ControleAcessoService controleAcessoService) {
        this.controleAcessoService = controleAcessoService;
    }

    @RequerPermissao(
        value = {
            PermissaoSistema.AREA_ADMIN_ACESSO,
            PermissaoSistema.GERENCIAR_PERMISSOES
        },
        somenteAdmin = true
    )
    @GetMapping("/admin/access-control/roles")
    public ResponseEntity<List<PerfilPermissaoResponseDTO>> listarPerfis(
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {

        return ResponseEntity.ok(
            controleAcessoService.listarPerfisEPermissoes(authorization)
        );
    }

    @RequerPermissao(
        value = {
            PermissaoSistema.AREA_ADMIN_ACESSO,
            PermissaoSistema.GERENCIAR_PERMISSOES
        },
        somenteAdmin = true
    )
    @PutMapping("/admin/access-control/roles/{role}")
    public ResponseEntity<PerfilPermissaoResponseDTO> atualizarPermissoes(
            @PathVariable String role,
            @Valid @RequestBody AtualizarPermissoesRequestDTO dto,
            @RequestHeader(value = "Authorization", required = false)
            String authorization,
            HttpServletRequest request) {

        UserRole perfil = converterPerfil(role);

        return ResponseEntity.ok(
            controleAcessoService.atualizarPermissoesPerfil(
                perfil,
                dto,
                authorization,
                request.getRemoteAddr()
            )
        );
    }

    @GetMapping("/auth/permissions")
    public ResponseEntity<PermissoesUsuarioResponseDTO> obterPermissoes(
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {

        return ResponseEntity.ok(
            controleAcessoService.obterPermissoesDoUsuarioAutenticado(
                authorization
            )
        );
    }

    private UserRole converterPerfil(String role) {
        try {
            return UserRole.valueOf(role);
        } catch (IllegalArgumentException exception) {
            throw new RegraNegocioException(
                HttpStatus.BAD_REQUEST,
                "PERMISSAO_INVALIDA",
                "O perfil informado é inválido."
            );
        }
    }
}