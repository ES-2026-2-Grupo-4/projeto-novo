package com.esproject4.arenaufcg.Model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Service.RegraNegocioException;

import jakarta.persistence.*;

@Entity
@Table(name = "venues")
public class Local {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", length = 120, nullable = false)
    private String nome;

    @Column(name = "address", length = 255)
    private String endereco;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false, columnDefinition = "VARCHAR(20)")
    private TipoLocal tipo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    protected Local() {}

    public Local(String nome, String endereco, TipoLocal tipo) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS",
                "O nome do local é obrigatório.");
        }
        if (tipo == null) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS",
                "O tipo do local é obrigatório.");
        }
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public TipoLocal getTipo() {
        return tipo;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }
}
