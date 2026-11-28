package com.esproject4.arenaufcg.Model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Um atleta dentro de uma escalação. Não tem id próprio: só existe dentro da Escalacao
// (cada linha da tabela lineup_players pertence a uma escalação).
@Embeddable
public class AtletaEscalado {

    @Column(name = "athlete_id", nullable = false)
    private UUID atletaId;

    @Column(name = "shirt_number")
    private Integer numero;

    @Column(name = "match_position", length = 30)
    private String posicaoNaPartida;

    @Column(name = "starter", nullable = false)
    private boolean titular;

    protected AtletaEscalado() {
    }

    public AtletaEscalado(UUID atletaId, Integer numero, String posicaoNaPartida, boolean titular) {
        this.atletaId = atletaId;
        this.numero = numero;
        this.posicaoNaPartida = posicaoNaPartida;
        this.titular = titular;
    }

    // Só a Escalacao altera um atleta escalado (por isso não é public).
    void alterar(Integer numero, String posicaoNaPartida, boolean titular) {
        this.numero = numero;
        this.posicaoNaPartida = posicaoNaPartida;
        this.titular = titular;
    }

    public UUID getAtletaId() {
        return atletaId;
    }

    public Integer getNumero() {
        return numero;
    }

    public String getPosicaoNaPartida() {
        return posicaoNaPartida;
    }

    public boolean isTitular() {
        return titular;
    }
}
