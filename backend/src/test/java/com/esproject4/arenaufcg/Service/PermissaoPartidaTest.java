package com.esproject4.arenaufcg.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.Campeonato;
import com.esproject4.arenaufcg.Model.FormatoDisputa;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Model.Partida;
import com.esproject4.arenaufcg.Model.Time;

class PermissaoPartidaTest {

    private final PermissaoPartida permissao = new PermissaoPartida();

    private UUID dirigenteMandanteId;
    private UUID dirigenteVisitanteId;
    private Time mandante;
    private Time visitante;
    private Partida partida;

    @BeforeEach
    void setUp() {
        Campeonato campeonato = new Campeonato("Campeonato Universitário", null, Modalidade.FUTSAL,
                FormatoDisputa.PONTOS_CORRIDOS, null, 4,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(40), UUID.randomUUID());

        dirigenteMandanteId = UUID.randomUUID();
        dirigenteVisitanteId = UUID.randomUUID();
        mandante = time("Computação", "CC", dirigenteMandanteId);
        visitante = time("Engenharia", "ENG", dirigenteVisitanteId);

        partida = new Partida(campeonato, mandante, visitante, OffsetDateTime.now().plusDays(2), null);
    }

    @Test
    void devePermitirDirigenteDoMandante() {
        assertSame(mandante, permissao.exigirDirigenteDoTime(partida, mandante.getId(), dirigenteMandanteId));
    }

    @Test
    void devePermitirDirigenteDoVisitante() {
        assertSame(visitante, permissao.exigirDirigenteDoTime(partida, visitante.getId(), dirigenteVisitanteId));
    }

    @Test
    void deveNegarDirigenteDeTimeForaDaPartida() {
        UUID dirigenteDeFora = UUID.randomUUID();
        Time deFora = time("Medicina", "MED", dirigenteDeFora);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> permissao.exigirDirigenteDoTime(partida, deFora.getId(), dirigenteDeFora));
        assertEquals(HttpStatus.FORBIDDEN, erro.getStatus());
        assertEquals("ACESSO_NEGADO", erro.getCodigo());
    }

    @Test
    void deveNegarDirigenteDoOutroTimeDaPartida() {
        // O dirigente do visitante tentando mexer na escalação do mandante.
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> permissao.exigirDirigenteDoTime(partida, mandante.getId(), dirigenteVisitanteId));
        assertEquals("ACESSO_NEGADO", erro.getCodigo());
    }

    @Test
    void devePermitirMandante() {
        assertSame(mandante, permissao.exigirMandante(partida, dirigenteMandanteId));
    }

    @Test
    void deveNegarQuemNaoEMandante() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> permissao.exigirMandante(partida, dirigenteVisitanteId));
        assertEquals(HttpStatus.FORBIDDEN, erro.getStatus());
        assertEquals("ACESSO_NEGADO", erro.getCodigo());
    }

    private static Time time(String nome, String sigla, UUID dirigenteId) {
        Time time = new Time(nome, sigla, Modalidade.FUTSAL, dirigenteId);
        ReflectionTestUtils.setField(time, "id", UUID.randomUUID());
        return time;
    }
}
