package com.esproject4.arenaufcg.Model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;

import jakarta.persistence.*;

@Entity
@Table(name = "roster_entries")
public class AtletaElenco {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "registration_id", nullable = false, updatable = false)
    private Inscricao inscricao;

    @Column(name = "championship_id", nullable = false, updatable = false)
    private UUID campeonatoId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "athlete_id", nullable = false, updatable = false)
    private Atleta atleta;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "number", nullable = false)
    private int numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", length = 20, nullable = false, columnDefinition = "VARCHAR(20)")
    private Posicao posicao;

    @ColumnDefault("true")
    @Column(name = "active", nullable = false)
    private boolean ativo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime atualizadoEm;

    protected AtletaElenco() {}

    public AtletaElenco(Inscricao inscricao, Atleta atleta, int numero, Posicao posicao) {
        if (inscricao == null) {
            throw dadosInvalidos("A inscrição é obrigatória.");
        }
        if (atleta == null) {
            throw dadosInvalidos("O atleta é obrigatório.");
        }
        if (!atleta.pertenceAoTime(inscricao.getTime().getId())) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "ATLETA_DE_OUTRO_TIME",
                    "O atleta não é do time da inscrição.");
        }
        validar(inscricao, numero, posicao);

        this.inscricao = inscricao;
        this.campeonatoId = inscricao.getCampeonato().getId();
        this.atleta = atleta;
        this.numero = numero;
        this.posicao = posicao;
        this.ativo = true;
    }

    public void atualizar(int numero, Posicao posicao) {
        validar(this.inscricao, numero, posicao);

        this.numero = numero;
        this.posicao = posicao;
    }

    public void remover() {
        this.ativo = false;
    }

    private static void validar(Inscricao inscricao, int numero, Posicao posicao) {
        if (posicao == null) {
            throw dadosInvalidos("A posição é obrigatória.");
        }
        if (!posicao.aceita(inscricao.getTime().getModalidade())) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "POSICAO_INVALIDA",
                    "A posição não é da modalidade do time.");
        }
        if (!inscricao.getCampeonato().numeroPermitido(numero)) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "NUMERO_FORA_DA_FAIXA",
                    "O número está fora da faixa permitida pelo campeonato.");
        }
    }

    private static RegraNegocioException dadosInvalidos(String mensagem) {
        return new RegraNegocioException(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", mensagem);
    }

    public UUID getId() {
        return id;
    }

    public Inscricao getInscricao() {
        return inscricao;
    }

    public UUID getCampeonatoId() {
        return campeonatoId;
    }

    public Atleta getAtleta() {
        return atleta;
    }

    public int getNumero() {
        return numero;
    }

    public Posicao getPosicao() {
        return posicao;
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