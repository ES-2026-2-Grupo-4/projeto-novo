package com.esproject4.arenaufcg.Model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Service.RegraNegocioException;

import jakarta.persistence.*;

@Entity
@Table(name = "championships")
public class Campeonato {

    public static final int MIN_TIMES = 2;
    public static final int MAX_TIMES = 64;

    public static final int NUMERO_MINIMO_PADRAO = 1;
    public static final int NUMERO_MAXIMO_PADRAO = 99;
    public static final int NUMERO_LIMITE_INFERIOR = 0;
    public static final int NUMERO_LIMITE_SUPERIOR = 99;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", length = 120, nullable = false)
    private String nome;

    @Column(name = "description", length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "modality", length = 20, nullable = false, columnDefinition = "VARCHAR(20)")
    private Modalidade modalidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 20, nullable = false, columnDefinition = "VARCHAR(20)")
    private FormatoDisputa formato;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "groups_count")
    private Integer quantidadeGrupos;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "max_teams", nullable = false)
    private int maxTimes;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @ColumnDefault("0")
    @Column(name = "occupied_slots", nullable = false)
    private int vagasOcupadas;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @ColumnDefault("1")
    @Column(name = "min_number", nullable = false)
    private int numeroMinimo = NUMERO_MINIMO_PADRAO;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @ColumnDefault("99")
    @Column(name = "max_number", nullable = false)
    private int numeroMaximo = NUMERO_MAXIMO_PADRAO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false, columnDefinition = "VARCHAR(30)")
    private StatusCampeonato status;

    @Column(name = "start_date", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "end_date", nullable = false)
    private LocalDate dataFim;

    // FK para users(id). Fica como UUID enquanto a entidade User (T02) não estiver na master.
    @Column(name = "organizer_id", nullable = false, updatable = false)
    private UUID organizadorId;

    @ColumnDefault("true")
    @Column(name = "active", nullable = false)
    private boolean ativo;

    @Version
    @ColumnDefault("0")
    @Column(name = "version", nullable = false)
    private Long versao;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime atualizadoEm;

    @ManyToMany
    @JoinTable(
        name = "championship_venues",
        joinColumns = @JoinColumn(name = "championship_id"),
        inverseJoinColumns = @JoinColumn(name = "venue_id")
    )
    private Set<Local> locais = new HashSet<>();

    protected Campeonato() {}

    public Campeonato(String nome, String descricao, Modalidade modalidade, FormatoDisputa formato,
                      Integer quantidadeGrupos, int maxTimes, LocalDate dataInicio, LocalDate dataFim,
                      UUID organizadorId) {
        if (organizadorId == null) {
            throw dadosInvalidos("O organizador é obrigatório.");
        }
        validarDados(nome, modalidade, formato, quantidadeGrupos, maxTimes, dataInicio, dataFim);

        this.nome = nome;
        this.descricao = descricao;
        this.modalidade = modalidade;
        this.formato = formato;
        this.quantidadeGrupos = formato == FormatoDisputa.FASE_DE_GRUPOS ? quantidadeGrupos : null;
        this.maxTimes = maxTimes;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.organizadorId = organizadorId;
        this.vagasOcupadas = 0;
        this.ativo = true;
        this.status = StatusCampeonato.RASCUNHO;
    }

    // ---------- Edição ----------

    /**
     * Aplica a edição respeitando o status:
     * em RASCUNHO tudo pode mudar; com inscrições abertas ou encerradas, modalidade e formato
     * ficam fixos; depois do início, só nome e descrição mudam.
     */
    public void atualizar(String nome, String descricao, Modalidade modalidade, FormatoDisputa formato,
                          Integer quantidadeGrupos, int maxTimes, LocalDate dataInicio, LocalDate dataFim) {
        validarDados(nome, modalidade, formato, quantidadeGrupos, maxTimes, dataInicio, dataFim);

        Integer novosGrupos = formato == FormatoDisputa.FASE_DE_GRUPOS ? quantidadeGrupos : null;

        if (jaIniciou()) {
            boolean mudouOutroCampo = modalidade != this.modalidade
                || formato != this.formato
                || !Objects.equals(novosGrupos, this.quantidadeGrupos)
                || maxTimes != this.maxTimes
                || !dataInicio.equals(this.dataInicio)
                || !dataFim.equals(this.dataFim);
            if (mudouOutroCampo) {
                throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "CAMPO_BLOQUEADO",
                    "Depois do início, só nome e descrição podem ser alterados.");
            }
        } else if (status != StatusCampeonato.RASCUNHO
                && (modalidade != this.modalidade || formato != this.formato)) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "CAMPO_BLOQUEADO",
                "Modalidade e formato não podem mudar depois que as inscrições foram abertas.");
        }

        if (maxTimes < this.vagasOcupadas) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "MAX_TIMES_MENOR_QUE_VAGAS",
                "O número máximo de times não pode ser menor que as vagas já ocupadas.");
        }

        this.nome = nome;
        this.descricao = descricao;
        this.modalidade = modalidade;
        this.formato = formato;
        this.quantidadeGrupos = novosGrupos;
        this.maxTimes = maxTimes;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public void definirFaixaNumeros(Integer minimo, Integer maximo) {
        int novoMinimo = minimo != null ? minimo : this.numeroMinimo;
        int novoMaximo = maximo != null ? maximo : this.numeroMaximo;
        validarFaixaNumeros(novoMinimo, novoMaximo);

        if (status != StatusCampeonato.RASCUNHO
                && (novoMinimo != this.numeroMinimo || novoMaximo != this.numeroMaximo)) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "CAMPO_BLOQUEADO",
                "A faixa de números não pode mudar depois que as inscrições foram abertas.");
        }

        this.numeroMinimo = novoMinimo;
        this.numeroMaximo = novoMaximo;
    }

    public boolean numeroPermitido(int numero) {
        return numero >= numeroMinimo && numero <= numeroMaximo;
    }

    public void definirLocais(Collection<Local> novosLocais) {
        if (jaIniciou()) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "CAMPEONATO_INICIADO",
                "Os locais não podem ser alterados depois do início do campeonato.");
        }
        this.locais.clear();
        if (novosLocais != null) {
            this.locais.addAll(novosLocais);
        }
    }

    // ---------- Ciclo de vida ----------

    public void abrirInscricoes() {
        exigirStatus(StatusCampeonato.RASCUNHO, "Só é possível abrir inscrições de um campeonato em rascunho.");
        if (locais.isEmpty()) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "SEM_LOCAL",
                "Associe ao menos um local antes de abrir as inscrições.");
        }
        this.status = StatusCampeonato.INSCRICOES_ABERTAS;
    }

    public void encerrarInscricoes() {
        exigirStatus(StatusCampeonato.INSCRICOES_ABERTAS, "As inscrições não estão abertas.");
        this.status = StatusCampeonato.INSCRICOES_ENCERRADAS;
    }

    public void iniciar() {
        exigirStatus(StatusCampeonato.INSCRICOES_ENCERRADAS,
            "Só é possível iniciar um campeonato com as inscrições encerradas.");
        this.status = StatusCampeonato.EM_ANDAMENTO;
    }

    public void finalizar() {
        exigirStatus(StatusCampeonato.EM_ANDAMENTO, "Só é possível finalizar um campeonato em andamento.");
        this.status = StatusCampeonato.FINALIZADO;
    }

    /**
     * Desativação lógica. A verificação de inscrições pendentes/aprovadas (409)
     * fica no CampeonatoService, que consulta as inscrições.
     */
    public void remover() {
        if (jaIniciou()) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "CAMPEONATO_INICIADO",
                "Não é possível remover um campeonato que já começou.");
        }
        this.ativo = false;
    }

    public boolean aceitaInscricoes() {
        return this.status == StatusCampeonato.INSCRICOES_ABERTAS;
    }

    public boolean jaIniciou() {
        return this.status == StatusCampeonato.EM_ANDAMENTO || this.status == StatusCampeonato.FINALIZADO;
    }

    public boolean pertenceA(UUID usuarioId) {
        return usuarioId != null && usuarioId.equals(this.organizadorId);
    }

    // ---------- Vagas (mesmo comportamento usado pela T05) ----------

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

    // ---------- Validações ----------

    private static void validarDados(String nome, Modalidade modalidade, FormatoDisputa formato,
                                     Integer quantidadeGrupos, int maxTimes,
                                     LocalDate dataInicio, LocalDate dataFim) {
        if (nome == null || nome.isBlank()) {
            throw dadosInvalidos("O nome é obrigatório.");
        }
        if (modalidade == null || formato == null) {
            throw dadosInvalidos("Modalidade e formato são obrigatórios.");
        }
        if (dataInicio == null || dataFim == null) {
            throw dadosInvalidos("As datas de início e de término são obrigatórias.");
        }
        if (maxTimes < MIN_TIMES || maxTimes > MAX_TIMES) {
            throw dadosInvalidos("O número máximo de times deve estar entre " + MIN_TIMES + " e " + MAX_TIMES + ".");
        }
        if (dataFim.isBefore(dataInicio)) {
            throw dadosInvalidos("A data de término não pode ser anterior à data de início.");
        }
        if (formato == FormatoDisputa.FASE_DE_GRUPOS
                && (quantidadeGrupos == null || quantidadeGrupos < 1 || quantidadeGrupos > maxTimes)) {
            throw dadosInvalidos("Informe uma quantidade de grupos válida para a fase de grupos.");
        }
    }

    private static void validarFaixaNumeros(int minimo, int maximo) {
        if (minimo < NUMERO_LIMITE_INFERIOR || minimo > NUMERO_LIMITE_SUPERIOR
                || maximo < NUMERO_LIMITE_INFERIOR || maximo > NUMERO_LIMITE_SUPERIOR) {
            throw faixaInvalida("A faixa de números deve estar entre " + NUMERO_LIMITE_INFERIOR
                + " e " + NUMERO_LIMITE_SUPERIOR + ".");
        }
        if (minimo > maximo) {
            throw faixaInvalida("O número mínimo não pode ser maior que o número máximo.");
        }
    }

    private void exigirStatus(StatusCampeonato esperado, String mensagem) {
        if (this.status != esperado) {
            throw new RegraNegocioException(HttpStatus.CONFLICT, "TRANSICAO_INVALIDA", mensagem);
        }
    }

    private static RegraNegocioException faixaInvalida(String mensagem) {
        return new RegraNegocioException(HttpStatus.BAD_REQUEST, "FAIXA_DE_NUMEROS_INVALIDA", mensagem);
    }

    private static RegraNegocioException dadosInvalidos(String mensagem) {
        return new RegraNegocioException(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", mensagem);
    }

    // ---------- Getters ----------

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public Modalidade getModalidade() {
        return modalidade;
    }

    public FormatoDisputa getFormato() {
        return formato;
    }

    public Integer getQuantidadeGrupos() {
        return quantidadeGrupos;
    }

    public int getMaxTimes() {
        return maxTimes;
    }

    public int getVagasOcupadas() {
        return vagasOcupadas;
    }

    public int getNumeroMinimo() {
        return numeroMinimo;
    }

    public int getNumeroMaximo() {
        return numeroMaximo;
    }

    public StatusCampeonato getStatus() {
        return status;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public UUID getOrganizadorId() {
        return organizadorId;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Long getVersao() {
        return versao;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public OffsetDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public Set<Local> getLocais() {
        return Collections.unmodifiableSet(locais);
    }
}