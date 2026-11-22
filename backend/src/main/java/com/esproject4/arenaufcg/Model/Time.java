package com.esproject4.arenaufcg.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "teams")
public class Time {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 80)
    private String nome;

    @Column(name = "acronym", nullable = false, length = 5)
    private String sigla;

    @Enumerated(EnumType.STRING)
    @Column(name = "modality", nullable = false, length = 20)
    private Modalidade modalidade;

    @Column(name = "manager_id", nullable = false)
    private UUID dirigenteId;

    @Column(name = "active", nullable = false)
    private boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected Time() {
    }

    public Time(String nome, String sigla, Modalidade modalidade, UUID dirigenteId) {
        this.nome = nome;
        this.sigla = sigla;
        this.modalidade = modalidade;
        this.dirigenteId = dirigenteId;
    }

    // A modalidade não muda depois do cadastro (RN-25), por isso não há setModalidade.
    public void editar(String nome, String sigla) {
        this.nome = nome;
        this.sigla = sigla;
    }

    public boolean pertenceA(UUID usuarioId) {
        return dirigenteId != null && dirigenteId.equals(usuarioId);
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getSigla() {
        return sigla;
    }

    public Modalidade getModalidade() {
        return modalidade;
    }

    public UUID getDirigenteId() {
        return dirigenteId;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
