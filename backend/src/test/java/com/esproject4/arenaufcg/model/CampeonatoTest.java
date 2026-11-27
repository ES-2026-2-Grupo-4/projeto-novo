package com.esproject4.arenaufcg.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Model.Campeonato;
import com.esproject4.arenaufcg.Model.FormatoDisputa;
import com.esproject4.arenaufcg.Model.Local;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Model.StatusCampeonato;
import com.esproject4.arenaufcg.Model.TipoLocal;
import com.esproject4.arenaufcg.Service.RegraNegocioException;

class CampeonatoTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 12, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 12, 12);

    private UUID organizadorId;
    private Campeonato campeonato;
    private Local ginasio;

    @BeforeEach
    void setUp() {
        organizadorId = UUID.randomUUID();
        campeonato = novoCampeonato(FormatoDisputa.PONTOS_CORRIDOS, null, 4);
        ginasio = new Local("Ginásio da UFCG", "Rua Aprígio Veloso, 882", TipoLocal.GINASIO);
    }

    private Campeonato novoCampeonato(FormatoDisputa formato, Integer grupos, int maxTimes) {
        return new Campeonato("Copa UFCG de Futsal 2026", "Torneio entre os cursos do CEEI.",
            Modalidade.FUTSAL, formato, grupos, maxTimes, INICIO, FIM, organizadorId);
    }

    private static void assertRegra(HttpStatus status, String codigo, Executable acao) {
        RegraNegocioException ex = assertThrows(RegraNegocioException.class, acao);
        assertEquals(status, ex.getStatus());
        assertEquals(codigo, ex.getCodigo());
    }

    private void atualizarCom(Modalidade modalidade, FormatoDisputa formato, int maxTimes, LocalDate fim) {
        campeonato.atualizar("Novo nome", "Nova descrição", modalidade, formato, null, maxTimes, INICIO, fim);
    }

    private void levarAte(StatusCampeonato alvo) {
        campeonato.definirLocais(List.of(ginasio));
        campeonato.abrirInscricoes();
        if (alvo == StatusCampeonato.INSCRICOES_ABERTAS) return;
        campeonato.encerrarInscricoes();
        if (alvo == StatusCampeonato.INSCRICOES_ENCERRADAS) return;
        campeonato.iniciar();
        if (alvo == StatusCampeonato.EM_ANDAMENTO) return;
        campeonato.finalizar();
    }

    // ---------- Criação ----------

    @Test
    void deveCriarCampeonatoEmRascunho() {
        assertEquals(StatusCampeonato.RASCUNHO, campeonato.getStatus());
        assertEquals(organizadorId, campeonato.getOrganizadorId());
        assertTrue(campeonato.pertenceA(organizadorId));
        assertEquals(0, campeonato.getVagasOcupadas());
        assertTrue(campeonato.isAtivo());
        assertTrue(campeonato.getLocais().isEmpty());
        assertFalse(campeonato.aceitaInscricoes());
    }

    @Test
    void deveLancarExcecaoQuandoDadosForemInvalidos() {
        assertRegra(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", () -> new Campeonato(" ", null,
            Modalidade.FUTSAL, FormatoDisputa.MATA_MATA, null, 8, INICIO, FIM, organizadorId));
        assertRegra(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", () -> new Campeonato("Copa", null,
            null, FormatoDisputa.MATA_MATA, null, 8, INICIO, FIM, organizadorId));
        assertRegra(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", () -> new Campeonato("Copa", null,
            Modalidade.FUTSAL, FormatoDisputa.MATA_MATA, null, 8, null, FIM, organizadorId));
        assertRegra(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", () -> novoCampeonato(FormatoDisputa.MATA_MATA, null, 1));
        assertRegra(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", () -> novoCampeonato(FormatoDisputa.MATA_MATA, null, 65));
        assertRegra(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", () -> new Campeonato("Copa", null,
            Modalidade.FUTSAL, FormatoDisputa.MATA_MATA, null, 8, FIM, INICIO, organizadorId));
        assertRegra(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", () -> new Campeonato("Copa", null,
            Modalidade.FUTSAL, FormatoDisputa.MATA_MATA, null, 8, INICIO, FIM, null));
    }

    @Test
    void deveAceitarLimitesDeMaxTimes() {
        assertEquals(2, novoCampeonato(FormatoDisputa.MATA_MATA, null, 2).getMaxTimes());
        assertEquals(64, novoCampeonato(FormatoDisputa.MATA_MATA, null, 64).getMaxTimes());
    }

    @Test
    void deveExigirQuantidadeDeGruposNaFaseDeGrupos() {
        assertRegra(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS",
            () -> novoCampeonato(FormatoDisputa.FASE_DE_GRUPOS, null, 8));

        Campeonato comGrupos = novoCampeonato(FormatoDisputa.FASE_DE_GRUPOS, 2, 8);
        assertEquals(2, comGrupos.getQuantidadeGrupos());
    }

    @Test
    void deveIgnorarQuantidadeDeGruposForaDaFaseDeGrupos() {
        assertNull(novoCampeonato(FormatoDisputa.MATA_MATA, 2, 8).getQuantidadeGrupos());
    }

    // ---------- Edição ----------

    @Test
    void deveAtualizarCampeonatoEmRascunho() {
        campeonato.atualizar("Novo nome", "Nova descrição", Modalidade.VOLEI, FormatoDisputa.FASE_DE_GRUPOS,
            2, 16, INICIO.plusDays(1), FIM.plusDays(1));

        assertEquals("Novo nome", campeonato.getNome());
        assertEquals("Nova descrição", campeonato.getDescricao());
        assertEquals(Modalidade.VOLEI, campeonato.getModalidade());
        assertEquals(FormatoDisputa.FASE_DE_GRUPOS, campeonato.getFormato());
        assertEquals(2, campeonato.getQuantidadeGrupos());
        assertEquals(16, campeonato.getMaxTimes());
        assertEquals(INICIO.plusDays(1), campeonato.getDataInicio());
        assertEquals(FIM.plusDays(1), campeonato.getDataFim());
    }

    @Test
    void deveBloquearModalidadeEFormatoAposAbrirInscricoes() {
        levarAte(StatusCampeonato.INSCRICOES_ABERTAS);
        assertRegra(HttpStatus.UNPROCESSABLE_CONTENT, "CAMPO_BLOQUEADO",
            () -> atualizarCom(Modalidade.VOLEI, FormatoDisputa.PONTOS_CORRIDOS, 4, FIM));
        assertRegra(HttpStatus.UNPROCESSABLE_CONTENT, "CAMPO_BLOQUEADO",
            () -> atualizarCom(Modalidade.FUTSAL, FormatoDisputa.MATA_MATA, 4, FIM));

        campeonato.encerrarInscricoes();
        assertRegra(HttpStatus.UNPROCESSABLE_CONTENT, "CAMPO_BLOQUEADO",
            () -> atualizarCom(Modalidade.VOLEI, FormatoDisputa.PONTOS_CORRIDOS, 4, FIM));

        // os demais campos continuam editáveis
        atualizarCom(Modalidade.FUTSAL, FormatoDisputa.PONTOS_CORRIDOS, 8, FIM.plusDays(2));
        assertEquals(8, campeonato.getMaxTimes());
        assertEquals(FIM.plusDays(2), campeonato.getDataFim());
    }

    @Test
    void deveLancarExcecaoQuandoMaxTimesForMenorQueVagasOcupadas() {
        campeonato.reservarVaga();
        campeonato.reservarVaga();
        campeonato.reservarVaga();

        assertRegra(HttpStatus.UNPROCESSABLE_CONTENT, "MAX_TIMES_MENOR_QUE_VAGAS",
            () -> atualizarCom(Modalidade.FUTSAL, FormatoDisputa.PONTOS_CORRIDOS, 2, FIM));
        assertEquals(4, campeonato.getMaxTimes());
    }

    @Test
    void devePermitirApenasNomeEDescricaoAposInicio() {
        levarAte(StatusCampeonato.EM_ANDAMENTO);

        atualizarCom(Modalidade.FUTSAL, FormatoDisputa.PONTOS_CORRIDOS, 4, FIM);
        assertEquals("Novo nome", campeonato.getNome());
        assertEquals("Nova descrição", campeonato.getDescricao());

        assertRegra(HttpStatus.UNPROCESSABLE_CONTENT, "CAMPO_BLOQUEADO",
            () -> atualizarCom(Modalidade.FUTSAL, FormatoDisputa.PONTOS_CORRIDOS, 8, FIM));
        assertRegra(HttpStatus.UNPROCESSABLE_CONTENT, "CAMPO_BLOQUEADO",
            () -> atualizarCom(Modalidade.FUTSAL, FormatoDisputa.PONTOS_CORRIDOS, 4, FIM.plusDays(1)));
    }

    @Test
    void deveIdentificarODono() {
        assertTrue(campeonato.pertenceA(organizadorId));
        assertFalse(campeonato.pertenceA(UUID.randomUUID()));
        assertFalse(campeonato.pertenceA(null));
    }

    // ---------- Ciclo de vida ----------

    @Test
    void deveAbrirInscricoesQuandoHouverLocal() {
        campeonato.definirLocais(List.of(ginasio));
        campeonato.abrirInscricoes();

        assertEquals(StatusCampeonato.INSCRICOES_ABERTAS, campeonato.getStatus());
        assertTrue(campeonato.aceitaInscricoes());
    }

    @Test
    void deveLancarExcecaoAoAbrirInscricoesSemLocal() {
        assertRegra(HttpStatus.UNPROCESSABLE_CONTENT, "SEM_LOCAL", campeonato::abrirInscricoes);
        assertEquals(StatusCampeonato.RASCUNHO, campeonato.getStatus());
    }

    @Test
    void devePercorrerOCicloDeVida() {
        levarAte(StatusCampeonato.INSCRICOES_ABERTAS);

        campeonato.encerrarInscricoes();
        assertEquals(StatusCampeonato.INSCRICOES_ENCERRADAS, campeonato.getStatus());
        assertFalse(campeonato.aceitaInscricoes());
        assertFalse(campeonato.jaIniciou());

        campeonato.iniciar();
        assertEquals(StatusCampeonato.EM_ANDAMENTO, campeonato.getStatus());
        assertTrue(campeonato.jaIniciou());

        campeonato.finalizar();
        assertEquals(StatusCampeonato.FINALIZADO, campeonato.getStatus());
        assertTrue(campeonato.jaIniciou());
    }

    @Test
    void deveLancarExcecaoEmTransicaoInvalida() {
        assertRegra(HttpStatus.CONFLICT, "TRANSICAO_INVALIDA", campeonato::encerrarInscricoes);
        assertRegra(HttpStatus.CONFLICT, "TRANSICAO_INVALIDA", campeonato::iniciar);
        assertRegra(HttpStatus.CONFLICT, "TRANSICAO_INVALIDA", campeonato::finalizar);

        levarAte(StatusCampeonato.INSCRICOES_ABERTAS);
        assertRegra(HttpStatus.CONFLICT, "TRANSICAO_INVALIDA", campeonato::abrirInscricoes);
        assertRegra(HttpStatus.CONFLICT, "TRANSICAO_INVALIDA", campeonato::iniciar);
    }

    // ---------- Vagas ----------

    @Test
    void deveReservarELiberarVagas() {
        campeonato.reservarVaga();
        campeonato.reservarVaga();
        assertEquals(2, campeonato.getVagasOcupadas());

        campeonato.liberarVaga();
        assertEquals(1, campeonato.getVagasOcupadas());
    }

    @Test
    void deveLancarExcecaoQuandoVagasEsgotarem() {
        for (int i = 0; i < 4; i++) {
            campeonato.reservarVaga();
        }
        assertRegra(HttpStatus.UNPROCESSABLE_CONTENT, "VAGAS_ESGOTADAS", campeonato::reservarVaga);
        assertEquals(4, campeonato.getVagasOcupadas());
    }

    @Test
    void deveLancarExcecaoAoLiberarSemVagasOcupadas() {
        assertRegra(HttpStatus.CONFLICT, "TRANSICAO_INVALIDA", campeonato::liberarVaga);
        assertEquals(0, campeonato.getVagasOcupadas());
    }

    // ---------- Remoção ----------

    @Test
    void deveRemoverCampeonatoLogicamente() {
        campeonato.remover();
        assertFalse(campeonato.isAtivo());
    }

    @Test
    void deveLancarExcecaoAoRemoverCampeonatoIniciado() {
        levarAte(StatusCampeonato.EM_ANDAMENTO);
        assertRegra(HttpStatus.UNPROCESSABLE_CONTENT, "CAMPEONATO_INICIADO", campeonato::remover);
        assertTrue(campeonato.isAtivo());
    }

    // ---------- Locais ----------

    @Test
    void deveSubstituirLocaisDoCampeonato() {
        Local quadra = new Local("Quadra do CEEI", null, TipoLocal.QUADRA);
        Local campo = new Local("Campo da UFCG", null, TipoLocal.CAMPO);

        campeonato.definirLocais(List.of(ginasio, quadra));
        assertEquals(2, campeonato.getLocais().size());

        campeonato.definirLocais(List.of(campo));
        assertEquals(1, campeonato.getLocais().size());
        assertTrue(campeonato.getLocais().contains(campo));
    }

    @Test
    void deveLancarExcecaoAoDefinirLocaisAposInicio() {
        levarAte(StatusCampeonato.EM_ANDAMENTO);
        assertRegra(HttpStatus.UNPROCESSABLE_CONTENT, "CAMPEONATO_INICIADO",
            () -> campeonato.definirLocais(List.of()));
    }

    @Test
    void deveExigirNomeETipoDoLocal() {
        assertRegra(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", () -> new Local(" ", null, TipoLocal.CAMPO));
        assertRegra(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", () -> new Local("Campo", null, null));
    }

    // T06 - Teste 9 da especificação
    @Test
    void deveRecusarFaixaDeNumerosInvalida() {
        assertRegra(HttpStatus.BAD_REQUEST, "FAIXA_DE_NUMEROS_INVALIDA",
            () -> campeonato.definirFaixaNumeros(50, 10));
        assertRegra(HttpStatus.BAD_REQUEST, "FAIXA_DE_NUMEROS_INVALIDA",
            () -> campeonato.definirFaixaNumeros(-1, 10));
        assertRegra(HttpStatus.BAD_REQUEST, "FAIXA_DE_NUMEROS_INVALIDA",
            () -> campeonato.definirFaixaNumeros(1, 100));

        assertEquals(Campeonato.NUMERO_MINIMO_PADRAO, campeonato.getNumeroMinimo());
        assertEquals(Campeonato.NUMERO_MAXIMO_PADRAO, campeonato.getNumeroMaximo());

        campeonato.definirFaixaNumeros(0, 30);
        assertTrue(campeonato.numeroPermitido(0));
        assertTrue(campeonato.numeroPermitido(30));
        assertFalse(campeonato.numeroPermitido(31));
    }

    // T06 - Teste 10 da especificação
    @Test
    void deveBloquearMudancaDaFaixaComInscricoesAbertas() {
        campeonato.definirFaixaNumeros(1, 30);
        levarAte(StatusCampeonato.INSCRICOES_ABERTAS);

        assertRegra(HttpStatus.UNPROCESSABLE_CONTENT, "CAMPO_BLOQUEADO",
            () -> campeonato.definirFaixaNumeros(1, 50));
        assertEquals(30, campeonato.getNumeroMaximo());

        campeonato.definirFaixaNumeros(1, 30);
    }
}
