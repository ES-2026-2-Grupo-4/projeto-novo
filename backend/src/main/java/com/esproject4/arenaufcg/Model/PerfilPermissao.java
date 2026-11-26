package com.esproject4.arenaufcg.Model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "role_permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerfilPermissao {

    private static final Set<PermissaoSistema> PERMISSOES_ESSENCIAIS_ADMIN = Set.of(
        PermissaoSistema.AREA_ADMIN_ACESSO,
        PermissaoSistema.GERENCIAR_PERMISSOES,
        PermissaoSistema.GERENCIAR_ADMINS,
        PermissaoSistema.VISUALIZAR_LOGS
    );

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 30, nullable = false)
    private UserRole perfil;

    @Column(name = "description", length = 255, nullable = false)
    private String descricao;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", nullable = false)
    private Set<PermissaoSistema> permissoes;

    @Column(name = "updated_by")
    private UUID atualizadoPor;

    @Column(name = "updated_at", nullable = false)
    private Instant atualizadoEm;

    public PerfilPermissao(
            UserRole perfil,
            String descricao,
            Set<PermissaoSistema> permissoes) {

        this.perfil = perfil;
        this.descricao = descricao;

        validarPermissoes(permissoes);

        this.permissoes = new HashSet<>(permissoes);
        this.atualizadoEm = Instant.now();
    }

    public void atualizarPermissoes(
            Set<PermissaoSistema> novasPermissoes,
            UUID adminId) {

        validarPermissoes(novasPermissoes);

        this.permissoes = new HashSet<>(novasPermissoes);
        this.atualizadoPor = adminId;
        this.atualizadoEm = Instant.now();
    }

    public boolean possuiPermissao(PermissaoSistema permissao) {
        return this.permissoes.contains(permissao);
    }

    private void validarPermissoes(Set<PermissaoSistema> permissoes) {

        if (permissoes == null || permissoes.stream().anyMatch(p -> p == null)) {
            throw new RegraNegocioException(
                HttpStatus.BAD_REQUEST,
                "PERMISSAO_INVALIDA",
                "O conjunto de permissões é inválido."
            );
        }

        if (this.perfil == UserRole.ADMIN &&
                !permissoes.containsAll(PERMISSOES_ESSENCIAIS_ADMIN)) {

            throw new RegraNegocioException(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "PERFIL_ADMIN_IMUTAVEL",
                "As permissões essenciais do administrador não podem ser removidas."
            );
        }
    }

    public Set<PermissaoSistema> getPermissoes() {
        return Set.copyOf(this.permissoes);
    }
}