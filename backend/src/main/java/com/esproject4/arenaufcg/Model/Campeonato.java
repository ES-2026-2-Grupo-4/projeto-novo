package com.esproject4.arenaufcg.Model;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import org.springframework.http.HttpStatus;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "championships")
public class Campeonato {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nome;

    @Enumerated(EnumType.STRING)
    private Modalidade modalidade;

    @Enumerated(EnumType.STRING)
    private StatusCampeonato status = StatusCampeonato.INSCRICOES_ABERTAS;

    private int maxTimes;

    private int vagasOcupadas;

    private UUID organizadorId;

    @Version
    private Long versao;

    protected Campeonato() {}

    public Campeonato(String nome, Modalidade modalidade, int maxTimes, UUID organizadorId) {
        this.nome = nome;
        this.modalidade = modalidade;
        this.maxTimes = maxTimes;
        this.organizadorId = organizadorId;
        this.vagasOcupadas = 0;
        this.status = StatusCampeonato.INSCRICOES_ABERTAS;
    }

    public boolean aceitarInscricoes() {
        return this.status == StatusCampeonato.INSCRICOES_ABERTAS;
    }

    public void reservarVaga() {
        if (this.vagasOcupadas >= this.maxTimes) {
            throw new RegraNegocioException(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "VAGAS_ESGOTADAS",
                "Não há vagas disponíveis neste campeonato."
            );
        }

        this.vagasOcupadas++;
    }

    public void liberarVaga() {
        if (this.vagasOcupadas <= 0) {
            throw new RegraNegocioException(
                HttpStatus.CONFLICT,
                "TRANSICAO_INVALIDA",
                "Não existem vagas ocupadas para liberar."
            );
        }

        this.vagasOcupadas--;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Modalidade getModalidade() {
        return modalidade;
    }

    public StatusCampeonato getStatus() {
        return status;
    }

    public int getMaxTimes() {
        return maxTimes;
    }

    public int getVagasOcupadas() {
        return vagasOcupadas;
    }

    public UUID getOrganizadorId() {
        return organizadorId;
    }

    public Long getVersao() {
        return versao;
    }
}