package com.esproject4.arenaufcg.Service;

import com.esproject4.arenaufcg.DTO.InscricaoResponseDTO;
import com.esproject4.arenaufcg.Model.*;
import com.esproject4.arenaufcg.Repository.*;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class InscricaoDirigenteServiceTest {
    
    @Mock
    private InscricaoRepository inscricaoRepository;

    @Mock
    private CampeonatoRepository campeonatoRepository;

    @Mock
    private TimeRepository timeRepository;

    @InjectMocks
    private InscricaoDirigenteService service;
    private Campeonato campeonato;
    private Time time;
    private UUID dirigenteId;
    private UUID campeonatoId;
    private UUID timeId;

    @BeforeEach
    void setUp() {
        dirigenteId = UUID.randomUUID();
        campeonatoId = UUID.randomUUID();
        timeId = UUID.randomUUID();

        // No master, um campeonato novo começa em RASCUNHO; o spy finge inscrições abertas.
        campeonato = spy(new Campeonato(
                "Campeonato Universitario", null, Modalidade.FUTSAL, FormatoDisputa.PONTOS_CORRIDOS,
                null, 4, LocalDate.now().plusDays(10), LocalDate.now().plusDays(40), UUID.randomUUID()
        ));
        lenient().doReturn(true).when(campeonato).aceitaInscricoes();

        time = new Time(
                "Time Universitario",
                "TU",
                Modalidade.FUTSAL,
                dirigenteId
        );
    }

    /*
     * Verifica se uma solicitação de inscrição é criada
     * com o status inicial PENDENTE.
     */
    @Test
    void deveSolicitarInscricaoComStatusPendente() {

        when(campeonatoRepository.findById(campeonatoId))
                .thenReturn(Optional.of(campeonato));

        when(timeRepository.findById(timeId))
                .thenReturn(Optional.of(time));

        when(inscricaoRepository.existsByCampeonatoIdAndTimeIdAndStatusIn(
                eq(campeonatoId),
                eq(timeId),
                anyCollection()
        )).thenReturn(false);

        when(inscricaoRepository.save(any(Inscricao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InscricaoResponseDTO response = service.solicitar(
                campeonatoId,
                timeId,
                dirigenteId
        );

        assertNotNull(response);
        assertEquals(StatusInscricao.PENDENTE, response.status());

        verify(inscricaoRepository).save(any(Inscricao.class));
    }


    /*
     * Verifica se o sistema impede uma inscrição
     * quando as inscrições do campeonato estão fechadas.
     */
    @Test
    void naoDeveSolicitarInscricaoQuandoInscricoesEstaoFechadas() {

        Campeonato campeonatoFechado = mock(Campeonato.class);

        when(campeonatoRepository.findById(campeonatoId))
                .thenReturn(Optional.of(campeonatoFechado));

        when(timeRepository.findById(timeId))
                .thenReturn(Optional.of(time));

        when(campeonatoFechado.aceitaInscricoes())
                .thenReturn(false);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> service.solicitar(campeonatoId, timeId, dirigenteId)
        );

        assertEquals("INSCRICOES_FECHADAS", exception.getCodigo());
        assertEquals(
                org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                exception.getStatus()
        );

        verify(inscricaoRepository, never()).save(any(Inscricao.class));
    }


    /*
     * Verifica se o sistema impede a inscrição
     * quando a modalidade do time é diferente
     * da modalidade do campeonato.
     */
    @Test
    void naoDeveSolicitarInscricaoComModalidadeDiferente() {

        Campeonato campeonatoVolei = spy(new Campeonato(
                "Campeonato de Volei", null, Modalidade.VOLEI, FormatoDisputa.PONTOS_CORRIDOS,
                null, 4, LocalDate.now().plusDays(10), LocalDate.now().plusDays(40), UUID.randomUUID()
        ));
        lenient().doReturn(true).when(campeonatoVolei).aceitaInscricoes();

        when(campeonatoRepository.findById(campeonatoId))
                .thenReturn(Optional.of(campeonatoVolei));

        when(timeRepository.findById(timeId))
                .thenReturn(Optional.of(time));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> service.solicitar(campeonatoId, timeId, dirigenteId)
        );

        assertEquals("MODALIDADE_DIFERENTE", exception.getCodigo());
        assertEquals(
                org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                exception.getStatus()
        );

        verify(inscricaoRepository, never()).save(any(Inscricao.class));
    }

     /*
     * Verifica se o sistema impede uma inscrição duplicada
     * quando já existe uma inscrição ativa para o time
     * naquele campeonato.
     */
    @Test
    void naoDeveSolicitarInscricaoDuplicada() {

        when(campeonatoRepository.findById(campeonatoId))
                .thenReturn(Optional.of(campeonato));

        when(timeRepository.findById(timeId))
                .thenReturn(Optional.of(time));

        when(inscricaoRepository.existsByCampeonatoIdAndTimeIdAndStatusIn(
                eq(campeonatoId),
                eq(timeId),
                anyCollection()
        )).thenReturn(true);

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> service.solicitar(campeonatoId, timeId, dirigenteId)
        );

        assertEquals("INSCRICAO_ATIVA_EXISTENTE", exception.getCodigo());
        assertEquals(
                org.springframework.http.HttpStatus.CONFLICT,
                exception.getStatus()
        );

        verify(inscricaoRepository, never()).save(any(Inscricao.class));
    }

    /*
     * Verifica se um dirigente não consegue solicitar
     * inscrição utilizando um time pertencente a outra pessoa.
     */
    @Test
    void naoDeveSolicitarInscricaoEmTimeDeOutroDirigente() {

        when(campeonatoRepository.findById(campeonatoId))
                .thenReturn(Optional.of(campeonato));

        when(timeRepository.findById(timeId))
                .thenReturn(Optional.of(time));

        UUID outroDirigenteId = UUID.randomUUID();

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> service.solicitar(campeonatoId, timeId, outroDirigenteId)
        );

        assertEquals("ACESSO_NEGADO", exception.getCodigo());
        assertEquals(
                org.springframework.http.HttpStatus.FORBIDDEN,
                exception.getStatus()
        );

        verify(inscricaoRepository, never()).save(any(Inscricao.class));
    }

     /*
     * Verifica se uma nova solicitação pode ser feita
     * quando não existe inscrição ativa.
     *
     * O repositório consulta apenas os status PENDENTE
     * e APROVADA. Portanto, registros antigos RECUSADA
     * ou CANCELADA não devem bloquear uma nova solicitação.
     */
    @Test
    void devePermitirNovaSolicitacaoAposInscricaoRecusadaOuCancelada() {

        when(campeonatoRepository.findById(campeonatoId))
                .thenReturn(Optional.of(campeonato));

        when(timeRepository.findById(timeId))
                .thenReturn(Optional.of(time));

        when(inscricaoRepository.existsByCampeonatoIdAndTimeIdAndStatusIn(
                eq(campeonatoId),
                eq(timeId),
                anyCollection()
        )).thenReturn(false);

        when(inscricaoRepository.save(any(Inscricao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InscricaoResponseDTO response = service.solicitar(
                campeonatoId,
                timeId,
                dirigenteId
        );

        assertNotNull(response);
        assertEquals(StatusInscricao.PENDENTE, response.status());

        verify(inscricaoRepository).save(any(Inscricao.class));
    }

    /*
     * Verifica se o dirigente consegue cancelar
     * uma inscrição que está com status PENDENTE.
     */

    @Test
    void deveCancelarInscricaoPendente() {

        Inscricao inscricao = new Inscricao(campeonato, time);
        UUID inscricaoId = UUID.randomUUID();

        when(inscricaoRepository.findById(inscricaoId))
                .thenReturn(Optional.of(inscricao));

        when(inscricaoRepository.save(any(Inscricao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InscricaoResponseDTO response = service.cancelar(
                inscricaoId,
                dirigenteId
        );

        assertNotNull(response);
        assertEquals(StatusInscricao.CANCELADA, response.status());

        verify(inscricaoRepository).save(inscricao);
    }

    /*
     * Verifica se o dirigente não consegue cancelar
     * uma inscrição que já está APROVADA.
     */
    @Test
    void naoDeveCancelarInscricaoQueNaoEstaPendente() {

        Inscricao inscricao = new Inscricao(campeonato, time);
        inscricao.aprovar(UUID.randomUUID());

        UUID inscricaoId = UUID.randomUUID();

        when(inscricaoRepository.findById(inscricaoId))
                .thenReturn(Optional.of(inscricao));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> service.cancelar(inscricaoId, dirigenteId)
        );

        assertEquals("TRANSICAO_INVALIDA", exception.getCodigo());
        assertEquals(
                org.springframework.http.HttpStatus.CONFLICT,
                exception.getStatus()
        );

        verify(inscricaoRepository, never()).save(any(Inscricao.class));
    }

    /*
     * Verifica se o dirigente consegue consultar
     * as inscrições pertencentes ao seu time.
     */
    @Test
    void deveListarInscricoesDoTimeDoDirigente() {

        Inscricao inscricao = new Inscricao(campeonato, time);

        when(timeRepository.findById(timeId))
                .thenReturn(Optional.of(time));

        when(inscricaoRepository.findByTimeIdOrderBySolicitadaEmDesc(timeId))
                .thenReturn(java.util.List.of(inscricao));

        java.util.List<InscricaoResponseDTO> response = service.listar(
                timeId,
                dirigenteId
        );

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(StatusInscricao.PENDENTE, response.get(0).status());

        verify(inscricaoRepository)
                .findByTimeIdOrderBySolicitadaEmDesc(timeId);
    }
}
