package com.esproject4.arenaufcg.Model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;

import jakarta.persistence.*;

@Entity
@Table(name = "athletes")
public class Atleta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team_id", nullable = false, updatable = false)
    private Time time;

    @Column(name = "name", length = 120, nullable = false)
    private String nome;

    @Column(name = "nickname", length = 60)
    private String apelido;

    @Column(name = "birth_date")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_position", length = 20, columnDefinition = "VARCHAR(20)")
    private Posicao posicaoPreferida;

    @ColumnDefault("true")
    @Column(name = "active", nullable = false)
    private boolean ativo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime atualizadoEm;

    protected Atleta() {}

    public Atleta(Time time, String nome, String apelido, LocalDate dataNascimento,
                  Posicao posicaoPreferida) {
        if (time == null) {
            throw dadosInvalidos("O time é obrigatório.");
        }
        validarNome(nome);
        validarPosicao(time, posicaoPreferida);

        this.time = time;
        this.nome = nome;
        this.apelido = apelido;
        this.dataNascimento = dataNascimento;
        this.posicaoPreferida = posicaoPreferida;
        this.ativo = true;
    }

    public void atualizar(String nome, String apelido, LocalDate dataNascimento,
                          Posicao posicaoPreferida) {
        validarNome(nome);
        validarPosicao(this.time, posicaoPreferida);

        this.nome = nome;
        this.apelido = apelido;
        this.dataNascimento = dataNascimento;
        this.posicaoPreferida = posicaoPreferida;
    }

    public void remover() {
        this.ativo = false;
    }

    public boolean pertenceAoTime(UUID timeId) {
        return this.time != null && this.time.getId() != null
                && this.time.getId().equals(timeId);
    }

    private static void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw dadosInvalidos("O nome do atleta é obrigatório.");
        }
    }

    private static void validarPosicao(Time time, Posicao posicao) {
        if (posicao != null && !posicao.aceita(time.getModalidade())) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "POSICAO_INVALIDA",
                    "A posição não é da modalidade do time.");
        }
    }

    private static RegraNegocioException dadosInvalidos(String mensagem) {
        return new RegraNegocioException(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", mensagem);
    }

    public UUID getId() {
        return id;
    }

    public Time getTime() {
        return time;
    }

    public UUID getTimeId() {
        return time.getId();
    }

    public String getNome() {
        return nome;
    }

    public String getApelido() {
        return apelido;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public Posicao getPosicaoPreferida() {
        return posicaoPreferida;
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