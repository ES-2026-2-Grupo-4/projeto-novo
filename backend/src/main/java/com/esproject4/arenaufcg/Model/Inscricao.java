package com.esproject4.arenaufcg.Model;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity 
@Table(name = "registrations")
public class Inscricao {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false) @JoinColumn(name = "championship_id")
    private Campeonato campeonato;

    @ManyToOne(optional = false) @JoinColumn(name = "team_id")
    private Time time;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private StatusInscricao status;

    @Column(name = "reason", length = 500)
    private String motivo;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime solicitadaEm;

    @Column(name = "decided_at")
    private OffsetDateTime decididaEm;

    @Column(name = "decided_by")
    private UUID decididaPor;

    @Version
    private Long version;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected Inscricao(){}

    public Inscricao(Campeonato campeonato, Time time){
        this.campeonato = campeonato;
        this.time = time;
        this.status = StatusInscricao.PENDENTE;
        this.solicitadaEm = OffsetDateTime.now();
    }

    public void aprovar(UUID organizadorId){
        exigirStatus(StatusInscricao.PENDENTE);
        this.status = StatusInscricao.APROVADA;
        registrarDecisao(organizadorId);
    }

    public void recusar(String motivo, UUID organizadorId){
        exigirMotivo(motivo);
        exigirStatus(StatusInscricao.PENDENTE);
        this.status = StatusInscricao.RECUSADA;
        this.motivo = motivo;
        registrarDecisao(organizadorId);
    }


    public void cancelarPeloDirigente(){
        exigirStatus(StatusInscricao.PENDENTE);
        this.status = StatusInscricao.CANCELADA;
    }

    public void remover(String motivo){
        exigirMotivo(motivo);
        exigirStatus(StatusInscricao.APROVADA);
        this.status = StatusInscricao.REMOVIDA;
        this.motivo = motivo;
    }

    public void registrarDecisao(UUID organizadorId){
        this.decididaEm = OffsetDateTime.now();
        this.decididaPor = organizadorId;
    }

    public void exigirStatus(StatusInscricao esperando){
        if(status != esperando){
            throw new RegraNegocioException(HttpStatus.CONFLICT, "TRANSICAO_INVALIDA", "A inscrição está " + status + " e não permite esta ação.");
        }
    }

    private void exigirMotivo(String motivo){
        if(motivo == null || motivo.isBlank()){
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "MOTIVO_OBRIGATORIO", "Informe o motivo.");
        }
    }

    public UUID getId() {
        return id;
    }

    public Campeonato getCampeonato() {
        return campeonato;
    }

    public Time getTime() {
        return time;
    }

    public StatusInscricao getStatus() {
        return status;
    }

    public String getMotivo() {
        return motivo;
    }

    public OffsetDateTime getSolicitadaEm() {
        return solicitadaEm;
    }

    public OffsetDateTime getDecididaEm() {
        return decididaEm;
    }

    public UUID getDecididaPor() {
        return decididaPor;
    }

    public Long getVersion() {
        return version;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
