package com.esproject4.arenaufcg.DTO;

import java.util.Set;

import com.esproject4.arenaufcg.Model.PermissaoSistema;
import com.esproject4.arenaufcg.Model.UserRole;

public record PermissoesUsuarioResponseDTO(
    UserRole perfil,
    Set<PermissaoSistema> permissoes
) {}