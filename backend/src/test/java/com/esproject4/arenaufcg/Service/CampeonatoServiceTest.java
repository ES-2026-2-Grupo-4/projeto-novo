package com.esproject4.arenaufcg.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Model.*;
import com.esproject4.arenaufcg.Repository.CampeonatoRepository;
import com.esproject4.arenaufcg.Repository.LocalRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampeonatoServiceTest {

    @Mock
    private CampeonatoRepository campeonatoRepository;

    @Mock
    private LocalRepository localRepository;

    private CampeonatoService service;

    private final UUID donoId = UUID.randomUUID();
    private final UUID campeonatoId = UUID.randomUUID();

    private final LocalDate inicio = LocalDate.of(2027, 3, 1);
    private final LocalDate fim = LocalDate.of(2027, 3, 10);

    @BeforeEach
    void preparar() {
        service = new CampeonatoService(
            campeonatoRepository,
            localRepository
        );
    }

    @Test
    void criarDeveSalvarCampeonatoEmRascunho() {
        when(campeonatoRepository.save(any(Campeonato.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

        Campeonato resultado = service.criar(
            "Copa UFCG",
            "Campeonato universitário",
            Modalidade.FUTEBOL,
            FormatoDisputa.MATA_MATA,
            null,
            8,
            inicio,
            fim,
            donoId
        );

        assertAll(
            () -> assertEquals("Copa UFCG", resultado.getNome()),
            () -> assertEquals(
                StatusCampeonato.RASCUNHO, resultado.getStatus()
            ),
            () -> assertEquals(0, resultado.getVagasOcupadas()),
            () -> assertEquals(donoId, resultado.getOrganizadorId()),
            () -> assertTrue(resultado.isAtivo())
        );

        verify(campeonatoRepository).save(resultado);
    }

    @Test
    void buscarDeOutroDonoDeveNegarAcesso() {
        Campeonato campeonato = campeonatoExistente();
        UUID outroUsuario = UUID.randomUUID();

        RegraNegocioException erro = assertThrows(
            RegraNegocioException.class,
            () -> service.buscar(campeonatoId, outroUsuario)
        );

        assertEquals(HttpStatus.FORBIDDEN, erro.getStatus());
        assertEquals("ACESSO_NEGADO", erro.getCodigo());

        verify(campeonatoRepository, never())
            .save(any(Campeonato.class));
    }

    @Test
    void buscarInexistenteDeveInformarNaoEncontrado() {
        when(campeonatoRepository.findByIdAndAtivoTrue(campeonatoId))
            .thenReturn(Optional.empty());

        RegraNegocioException erro = assertThrows(
            RegraNegocioException.class,
            () -> service.buscar(campeonatoId, donoId)
        );

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatus());
        assertEquals("NAO_ENCONTRADO", erro.getCodigo());
    }

    @Test
    void abrirInscricoesSemLocalDeveFalharSemAlterarStatus() {
        Campeonato campeonato = campeonatoExistente();

        RegraNegocioException erro = assertThrows(
            RegraNegocioException.class,
            () -> service.abrirInscricoes(campeonatoId, donoId)
        );

        assertEquals(
            HttpStatus.UNPROCESSABLE_CONTENT, erro.getStatus()
        );
        assertEquals("LOCAIS_OBRIGATORIOS", erro.getCodigo());
        assertEquals(
            StatusCampeonato.RASCUNHO, campeonato.getStatus()
        );

        verify(campeonatoRepository, never())
            .save(any(Campeonato.class));
    }

    @Test
    void abrirInscricoesComLocalDeveAlterarStatusESalvar() {
        Campeonato campeonato = campeonatoExistente();

        campeonato.definirLocais(java.util.List.of(
            new Local("Campo UFCG", "Campus", TipoLocal.CAMPO)
        ));

        when(campeonatoRepository.save(campeonato))
            .thenReturn(campeonato);

        Campeonato resultado = service.abrirInscricoes(
            campeonatoId, donoId
        );

        assertEquals(
            StatusCampeonato.INSCRICOES_ABERTAS,
            resultado.getStatus()
        );

        verify(campeonatoRepository).save(campeonato);
    }

    @Test
    void atualizarModalidadeComInscricoesAbertasDeveFalhar() {
        Campeonato campeonato = campeonatoExistente();

        campeonato.definirLocais(java.util.List.of(
            new Local("Campo UFCG", "Campus", TipoLocal.CAMPO)
        ));
        campeonato.abrirInscricoes();

        RegraNegocioException erro = assertThrows(
            RegraNegocioException.class,
            () -> service.atualizar(
                campeonatoId,
                donoId,
                "Copa UFCG",
                "Descrição",
                Modalidade.VOLEI,
                FormatoDisputa.MATA_MATA,
                null,
                8,
                inicio,
                fim
            )
        );

        assertEquals("CAMPO_BLOQUEADO", erro.getCodigo());
        assertEquals(Modalidade.FUTEBOL, campeonato.getModalidade());

        verify(campeonatoRepository, never())
            .save(any(Campeonato.class));
    }

    private Campeonato campeonatoExistente() {
        Campeonato campeonato = new Campeonato(
            "Copa UFCG",
            "Descrição",
            Modalidade.FUTEBOL,
            FormatoDisputa.MATA_MATA,
            null,
            8,
            inicio,
            fim,
            donoId
        );

        when(campeonatoRepository.findByIdAndAtivoTrue(campeonatoId))
            .thenReturn(Optional.of(campeonato));

        return campeonato;
    }
}
