package com.esproject4.arenaufcg.Service;

import com.esproject4.arenaufcg.Model.*;
import com.esproject4.arenaufcg.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscricaoOrganizadorServiceTest {

    @Mock
    private InscricaoRepository inscricaoRepository;

    @Mock
    private CampeonatoRepository campeonatoRepository;

    @InjectMocks
    private InscricaoOrganizadorService service;

    private Campeonato campeonato;
    private Inscricao inscricao;

    private UUID organizadorId;
    private UUID inscricaoId;

    @BeforeEach
    void setUp() {
        organizadorId = UUID.randomUUID();
        inscricaoId = UUID.randomUUID();

        campeonato = new Campeonato(
            "Campeonato Universitário",
            Modalidade.FUTSAL,
            4,
            organizadorId
        );

        Time time = mock(Time.class);

        inscricao = new Inscricao(campeonato, time);
    }

    @Test
    void deveIncrementarVagaAoAprovarInscricao() {

        when(inscricaoRepository.findById(inscricaoId))
            .thenReturn(Optional.of(inscricao));

        service.aprovar(inscricaoId, organizadorId);

        assertEquals(1, campeonato.getVagasOcupadas());
        assertEquals(StatusInscricao.APROVADA, inscricao.getStatus());
    }

    @Test
    void naoDeveAprovarInscricaoQuandoVagasEsgotadas() {
        for (int i = 0; i < 4; i++) {
            campeonato.reservarVaga();
        }

        when(inscricaoRepository.findById(inscricaoId))
            .thenReturn(Optional.of(inscricao));

        RegraNegocioException exception = assertThrows(
            RegraNegocioException.class,
            () -> service.aprovar(inscricaoId, organizadorId)
        );

        assertEquals("VAGAS_ESGOTADAS", exception.getCodigo());
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatus());
        assertEquals(4, campeonato.getVagasOcupadas());
    }

    @Test
    void deveLiberarVagaAoRemoverInscricao() {
        inscricao.aprovar(organizadorId);
        campeonato.reservarVaga();

        when(inscricaoRepository.findById(inscricaoId))
            .thenReturn(Optional.of(inscricao));

        service.remover(inscricaoId, "Descumprimento das regras", organizadorId);

        assertEquals(0, campeonato.getVagasOcupadas());
        assertEquals(StatusInscricao.REMOVIDA, inscricao.getStatus());
        assertEquals("Descumprimento das regras", inscricao.getMotivo());
    }

    @Test
    void naoDeveAprovarInscricaoDeOutroOrganizador() {
        UUID outroOrganizadorId = UUID.randomUUID();

        when(inscricaoRepository.findById(inscricaoId))
            .thenReturn(Optional.of(inscricao));

        RegraNegocioException exception = assertThrows(
            RegraNegocioException.class,
            () -> service.aprovar(inscricaoId, outroOrganizadorId)
        );

        assertEquals("ACESSO_NEGADO", exception.getCodigo());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());

        assertEquals(StatusInscricao.PENDENTE, inscricao.getStatus());
        assertEquals(0, campeonato.getVagasOcupadas());
    }

    @Test
    void naoDeveRecusarInscricaoSemMotivo() {
        when(inscricaoRepository.findById(inscricaoId))
            .thenReturn(Optional.of(inscricao));

        RegraNegocioException exception = assertThrows(
            RegraNegocioException.class,
            () -> service.recusar(inscricaoId, "", organizadorId)
        );

        assertEquals("MOTIVO_OBRIGATORIO", exception.getCodigo());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        assertEquals(StatusInscricao.PENDENTE, inscricao.getStatus());
        assertEquals(0, campeonato.getVagasOcupadas());
    }
}