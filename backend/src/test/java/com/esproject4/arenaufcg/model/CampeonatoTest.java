package com.esproject4.arenaufcg.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Model.Campeonato;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

class CampeonatoTest {

    private Campeonato campeonato;

    @BeforeEach
    void setUp() {
        campeonato = new Campeonato(
            "Campeonato Universitário",
            Modalidade.FUTSAL,
            4,
            UUID.randomUUID()
        );
    }

    @Test
    void deveReservarVagaQuandoDisponivel() {
        campeonato.reservarVaga();

        assertEquals(1, campeonato.getVagasOcupadas());
    }

    @Test
    void naoDeveReservarVagaQuandoEsgotadas() {
        for (int i = 0; i < 4; i++) {
            campeonato.reservarVaga();
        }

        RegraNegocioException exception = assertThrows(
            RegraNegocioException.class,
            () -> campeonato.reservarVaga()
        );

        assertEquals("VAGAS_ESGOTADAS", exception.getCodigo());
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatus());
        assertEquals(4, campeonato.getVagasOcupadas());
    }

    @Test
    void deveLiberarVagaOcupada() {
        campeonato.reservarVaga();
        campeonato.reservarVaga();
        assertEquals(2, campeonato.getVagasOcupadas());

        campeonato.liberarVaga();
        assertEquals(1, campeonato.getVagasOcupadas());
    }

    @Test
    void naoDeveLiberarVagaQuandoNaoExistemVagasOcupadas() {
        RegraNegocioException exception = assertThrows(
            RegraNegocioException.class,
            () -> campeonato.liberarVaga()
        );

        assertEquals("TRANSICAO_INVALIDA", exception.getCodigo());
        assertEquals(0, campeonato.getVagasOcupadas());
    }
}