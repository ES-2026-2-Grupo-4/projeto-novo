package com.esproject4.arenaufcg.Model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lineups",
       uniqueConstraints = @UniqueConstraint(columnNames = {"match_id", "team_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Escalacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Partida partida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Time time;

    @Column(name = "formation", length = 10)
    private String formacao;   // nasce null: o dirigente escolhe depois

    @OneToMany(mappedBy = "escalacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AtletaEscalado> atletas = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime atualizadoEm;

    public Escalacao(Partida partida, Time time) {
        this.partida = partida;
        this.time = time;
    }


    // ---------- Passo 6: métodos da base (implementados) ----------

    /** true se agora já passou da data/hora da partida. */
    public boolean estaFechada(OffsetDateTime agora) {
        return agora.isAfter(partida.getDataHora());
    }

    public long contarTitulares() {
        return atletas.stream().filter(AtletaEscalado::isTitular).count();
    }

    public int contarRelacionados() {
        return atletas.size();
    }

    // ---------- Passo 5: stubs, cada colega preenche o seu ----------

    public void adicionarOuAlterarAtleta(/* parâmetros combinados com a Camila */) {
        throw new UnsupportedOperationException();
    }

    public void removerAtleta(/* parâmetros combinados com a Camila */) {
        throw new UnsupportedOperationException();
    }

    public void definirFormacao(String formacao, RegraModalidade regra) {
        throw new UnsupportedOperationException();   // João Victor preenche (HU-15)
    }

    // ---------- auditoria ----------

    @PrePersist
    void aoCriar() {
        criadoEm = atualizadoEm = OffsetDateTime.now();
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = OffsetDateTime.now();
    }
}