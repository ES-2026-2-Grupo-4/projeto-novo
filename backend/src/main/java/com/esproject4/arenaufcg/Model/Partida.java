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

// PROVISÓRIO (T08): versão mínima da Partida para a escalação funcionar.
// Quando a T07 entregar a Partida oficial, esta classe é substituída pela dela
// (manter os nomes: getMandante, getVisitante, getDataHora, getStatus, participa).
@Entity
@Table(name = "matches")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "championship_id", nullable = false, updatable = false)
    private Campeonato campeonato;

    @ManyToOne(optional = false)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Time mandante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Time visitante;

    @Column(name = "scheduled_at", nullable = false)
    private OffsetDateTime dataHora;

    @Column(name = "venue_id")
    private UUID localId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPartida status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected Partida() {
    }

    public Partida(Campeonato campeonato, Time mandante, Time visitante,
                   OffsetDateTime dataHora, UUID localId) {
        if (campeonato == null || mandante == null || visitante == null || dataHora == null) {
            throw dadosInvalidos("Campeonato, mandante, visitante e data/hora são obrigatórios.");
        }
        if (mandante == visitante
                || (mandante.getId() != null && mandante.getId().equals(visitante.getId()))) {
            throw dadosInvalidos("Mandante e visitante precisam ser times diferentes.");
        }
        this.campeonato = campeonato;
        this.mandante = mandante;
        this.visitante = visitante;
        this.dataHora = dataHora;
        this.localId = localId;
        this.status = StatusPartida.AGENDADA;
    }

    // true se o time é o mandante ou o visitante desta partida.
    public boolean participa(UUID timeId) {
        return ehMandante(timeId) || ehVisitante(timeId);
    }

    public boolean ehMandante(UUID timeId) {
        return timeId != null && timeId.equals(mandante.getId());
    }

    public boolean ehVisitante(UUID timeId) {
        return timeId != null && timeId.equals(visitante.getId());
    }

    private static RegraNegocioException dadosInvalidos(String mensagem) {
        return new RegraNegocioException(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", mensagem);
    }

    public UUID getId() {
        return id;
    }

    public Campeonato getCampeonato() {
        return campeonato;
    }

    public Time getMandante() {
        return mandante;
    }

    public Time getVisitante() {
        return visitante;
    }

    public OffsetDateTime getDataHora() {
        return dataHora;
    }

    public UUID getLocalId() {
        return localId;
    }

    public StatusPartida getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
