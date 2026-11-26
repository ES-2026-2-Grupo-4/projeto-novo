package com.esproject4.arenaufcg.Model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Service.RegraNegocioException;

import jakarta.persistence.*;

@Entity
@Table(name = "staff_members")
public class MembroStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // FK para teams(id). Fica como UUID enquanto a entidade Time (T05) não estiver na master.
    @Column(name = "team_id", nullable = false, updatable = false)
    private UUID timeId;

    @Column(name = "name", length = 120, nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 30, nullable = false, columnDefinition = "VARCHAR(30)")
    private FuncaoStaff funcao;

    @ColumnDefault("true")
    @Column(name = "active", nullable = false)
    private boolean ativo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime atualizadoEm;

    protected MembroStaff() {}

    public MembroStaff(UUID timeId, String nome, FuncaoStaff funcao) {
        if (timeId == null) {
            throw dadosInvalidos("O time é obrigatório.");
        }
        validar(nome, funcao);

        this.timeId = timeId;
        this.nome = nome;
        this.funcao = funcao;
        this.ativo = true;
    }

    public void atualizar(String nome, FuncaoStaff funcao) {
        validar(nome, funcao);

        this.nome = nome;
        this.funcao = funcao;
    }

    public void remover() {
        this.ativo = false;
    }

    public boolean pertenceAoTime(UUID timeId) {
        return this.timeId != null && this.timeId.equals(timeId);
    }

    private static void validar(String nome, FuncaoStaff funcao) {
        if (nome == null || nome.isBlank()) {
            throw dadosInvalidos("O nome do membro do staff é obrigatório.");
        }
        if (funcao == null) {
            throw dadosInvalidos("A função do membro do staff é obrigatória.");
        }
    }

    private static RegraNegocioException dadosInvalidos(String mensagem) {
        return new RegraNegocioException(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", mensagem);
    }

    public UUID getId() {
        return id;
    }

    public UUID getTimeId() {
        return timeId;
    }

    public String getNome() {
        return nome;
    }

    public FuncaoStaff getFuncao() {
        return funcao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public OffsetDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}