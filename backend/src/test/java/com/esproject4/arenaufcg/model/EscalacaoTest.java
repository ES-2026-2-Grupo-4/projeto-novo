package com.esproject4.arenaufcg.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.AtletaEscalado;
import com.esproject4.arenaufcg.Model.Campeonato;
import com.esproject4.arenaufcg.Model.Escalacao;
import com.esproject4.arenaufcg.Model.FormatoDisputa;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Model.Partida;
import com.esproject4.arenaufcg.Model.Time;

class EscalacaoTest {

    private OffsetDateTime horarioPartida;
    private Time mandante;
    private Time visitante;
    private Partida partida;
    private Escalacao escalacao;

    @BeforeEach
    void setUp() {
        Campeonato campeonato = new Campeonato("Campeonato Universitário", null, Modalidade.FUTSAL,
                FormatoDisputa.PONTOS_CORRIDOS, null, 4,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(40), UUID.randomUUID());

        mandante = time("Computação", "CC");
        visitante = time("Engenharia", "ENG");

        horarioPartida = OffsetDateTime.now().plusDays(2);
        partida = new Partida(campeonato, mandante, visitante, horarioPartida, null);
        escalacao = new Escalacao(partida, mandante);
    }

    @Test
    void deveConsiderarEscalacaoFechadaAposHorarioDaPartida() {
        assertTrue(escalacao.estaFechada(horarioPartida.plusMinutes(1)));
    }

    @Test
    void deveConsiderarEscalacaoFechadaNoHorarioExatoDaPartida() {
        assertTrue(escalacao.estaFechada(horarioPartida));
    }

    @Test
    void deveConsiderarEscalacaoAbertaAntesDoHorario() {
        assertFalse(escalacao.estaFechada(horarioPartida.minusMinutes(1)));
    }

    @Test
    void deveLancarEscalacaoFechadaAoExigirAbertaDepoisDoHorario() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> escalacao.exigirAberta(horarioPartida.plusMinutes(1)));
        assertEquals("ESCALACAO_FECHADA", erro.getCodigo());
    }

    @Test
    void deveContarTitularesEReservas() {
        @SuppressWarnings("unchecked")
        List<AtletaEscalado> atletas = (List<AtletaEscalado>) ReflectionTestUtils.getField(escalacao, "atletas");
        atletas.add(new AtletaEscalado(UUID.randomUUID(), 1, "GOLEIRO", true));
        atletas.add(new AtletaEscalado(UUID.randomUUID(), 2, "FIXO", true));
        atletas.add(new AtletaEscalado(UUID.randomUUID(), 3, "ALA", true));
        atletas.add(new AtletaEscalado(UUID.randomUUID(), 12, "GOLEIRO", false));

        assertEquals(3, escalacao.contarTitulares());
        assertEquals(4, escalacao.contarRelacionados());
    }

    @Test
    void deveRecusarTimeQueNaoJogaAPartida() {
        Time outro = time("Medicina", "MED");

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> new Escalacao(partida, outro));
        assertEquals("TIME_FORA_DA_PARTIDA", erro.getCodigo());
    }

    private static Time time(String nome, String sigla) {
        Time time = new Time(nome, sigla, Modalidade.FUTSAL, UUID.randomUUID());
        ReflectionTestUtils.setField(time, "id", UUID.randomUUID());
        return time;
    }
}
