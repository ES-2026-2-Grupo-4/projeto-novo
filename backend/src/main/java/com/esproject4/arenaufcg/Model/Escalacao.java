package com.esproject4.arenaufcg.Model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

// Escalação de UM time para UMA partida (RN: uma escalação por time por partida).
// Fica aberta até o horário da partida; depois disso, congela.
@Entity
@Table(name = "lineups", uniqueConstraints = @UniqueConstraint(
        name = "uk_lineups_match_team", columnNames = {"match_id", "team_id"}))
public class Escalacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match_id", nullable = false, updatable = false)
    private Partida partida;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team_id", nullable = false, updatable = false)
    private Time time;

    @Column(name = "formation", length = 20)
    private String formacao;

    @ElementCollection
    @CollectionTable(name = "lineup_players",
            joinColumns = @JoinColumn(name = "lineup_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_lineup_players_athlete", columnNames = {"lineup_id", "athlete_id"}))
    private List<AtletaEscalado> atletas = new ArrayList<>();

    // Dois dirigentes salvando ao mesmo tempo: o segundo recebe 409 (CONFLITO_DE_VERSAO).
    @Version
    @Column(name = "version")
    private Long versao;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected Escalacao() {
    }

    public Escalacao(Partida partida, Time time) {
        if (partida == null || time == null) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS",
                    "Partida e time são obrigatórios.");
        }
        if (!partida.participa(time.getId())) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "TIME_FORA_DA_PARTIDA",
                    "Este time não joga esta partida.");
        }
        this.partida = partida;
        this.time = time;
    }

    // Fecha no horário da partida (inclusive) ou se a partida já saiu de AGENDADA.
    public boolean estaFechada(OffsetDateTime agora) {
        return partida.getStatus() != StatusPartida.AGENDADA
                || !agora.isBefore(partida.getDataHora());
    }

    // Use no começo de todo método que altera a escalação.
    public void exigirAberta(OffsetDateTime agora) {
        if (estaFechada(agora)) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "ESCALACAO_FECHADA",
                    "A escalação fechou no horário da partida e não pode mais ser alterada.");
        }
    }

    public OffsetDateTime getFechaEm() {
        return partida.getDataHora();
    }

    public int contarTitulares() {
        return (int) atletas.stream().filter(AtletaEscalado::isTitular).count();
    }

    // Relacionados = titulares + reservas.
    public int contarRelacionados() {
        return atletas.size();
    }

    public Optional<AtletaEscalado> buscarAtleta(UUID atletaId) {
        return atletas.stream().filter(a -> a.getAtletaId().equals(atletaId)).findFirst();
    }

    // =====================================================================
    // t08/atletas: preencher (apagar o throw e escrever a regra)
    // =====================================================================

    public void adicionarOuAlterarAtleta(UUID atletaId, Integer numero, String posicaoNaPartida,
                                         boolean titular, OffsetDateTime agora) {
        throw new UnsupportedOperationException("t08/atletas: a implementar");
    }

    public void removerAtleta(UUID atletaId, OffsetDateTime agora) {
        throw new UnsupportedOperationException("t08/atletas: a implementar");
    }

    // =====================================================================
    // t08/formacao: preencher (apagar o throw e escrever a regra)
    // =====================================================================

    public void definirFormacao(String formacao, OffsetDateTime agora) {
        throw new UnsupportedOperationException("t08/formacao: a implementar");
    }

    // =====================================================================
    // Getters
    // =====================================================================

    public UUID getId() {
        return id;
    }

    public Partida getPartida() {
        return partida;
    }

    public Time getTime() {
        return time;
    }

    public String getFormacao() {
        return formacao;
    }

    // Lista só de leitura: alterações passam pelos métodos acima.
    public List<AtletaEscalado> getAtletas() {
        return Collections.unmodifiableList(atletas);
    }

    public Long getVersao() {
        return versao;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
