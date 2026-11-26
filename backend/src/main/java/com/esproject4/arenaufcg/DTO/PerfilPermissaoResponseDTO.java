package com.esproject4.arenaufcg.DTO;

import java.time.Instant;
import java.util.Set;

import com.esproject4.arenaufcg.Model.PerfilPermissao;
import com.esproject4.arenaufcg.Model.PermissaoSistema;
import com.esproject4.arenaufcg.Model.UserRole;

public record PerfilPermissaoResponseDTO(
    UserRole perfil,
    String descricao,
    Set<PermissaoSistema> permissoes,
    Instant atualizadoEm
) {

    public static PerfilPermissaoResponseDTO de(PerfilPermissao perfil) {
        return new PerfilPermissaoResponseDTO(
            perfil.getPerfil(),
            perfil.getDescricao(),
            perfil.getPermissoes(),
            perfil.getAtualizadoEm()
        );
    }
}