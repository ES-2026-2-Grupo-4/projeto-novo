package com.esproject4.arenaufcg.Service;

import com.esproject4.arenaufcg.DTO.AtualizarElencoRequest;
import com.esproject4.arenaufcg.DTO.ElencoRequest;
import com.esproject4.arenaufcg.DTO.ElencoResponse;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.Atleta;
import com.esproject4.arenaufcg.Model.AtletaElenco;
import com.esproject4.arenaufcg.Model.Campeonato;
import com.esproject4.arenaufcg.Model.FormatoDisputa;
import com.esproject4.arenaufcg.Model.Inscricao;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Model.Posicao;
import com.esproject4.arenaufcg.Model.StatusCampeonato;
import com.esproject4.arenaufcg.Model.StatusInscricao;
import com.esproject4.arenaufcg.Model.Time;
import com.esproject4.arenaufcg.Repository.AtletaElencoRepository;
import com.esproject4.arenaufcg.Repository.AtletaRepository;
import com.esproject4.arenaufcg.Repository.CampeonatoRepository;
import com.esproject4.arenaufcg.Repository.InscricaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElencoServiceTest {

    @Mock
    private InscricaoRepository inscricaoRepository;

    @Mock
    private AtletaRepository atletaRepository;

    @Mock
    private AtletaElencoRepository elencoRepository;

    @Mock
    private CampeonatoRepository campeonatoRepository;

    @InjectMocks
    private ElencoService elencoService;

    private UUID dirigenteId;
    private UUID organizadorId;
    private UUID outroUsuarioId;
    private Time time;
    private Campeonato campeonato;
    private Inscricao inscricao;
    private Atleta atleta;

    @BeforeEach
    void setUp() {
        dirigenteId = UUID.randomUUID();
        organizadorId = UUID.randomUUID();
        outroUsuarioId = UUID.randomUUID();

        time = new Time("Computação FC", "CCFC", Modalidade.FUTSAL, dirigenteId);
        ReflectionTestUtils.setField(time, "id", UUID.randomUUID());

        campeonato = new Campeonato("Copa UFCG de Futsal 2026", "Torneio entre os cursos do CEEI.",
                Modalidade.FUTSAL, FormatoDisputa.PONTOS_CORRIDOS, null, 8,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 12), organizadorId);
        ReflectionTestUtils.setField(campeonato, "id", UUID.randomUUID());
        campeonato.definirFaixaNumeros(1, 20);

        inscricao = new Inscricao(campeonato, time);
        ReflectionTestUtils.setField(inscricao, "id", UUID.randomUUID());

        atleta = novoAtleta(time);

        lenient().when(campeonatoRepository.bloquearPorInscricao(inscricao.getId())).thenReturn(Optional.of(campeonato));
        lenient().when(inscricaoRepository.findById(inscricao.getId())).thenReturn(Optional.of(inscricao));
        lenient().when(atletaRepository.findByIdAndAtivoTrue(atleta.getId())).thenReturn(Optional.of(atleta));
    }

    private static Atleta novoAtleta(Time timeDoAtleta) {
        Atleta novo = new Atleta(timeDoAtleta, "Pedro Silva", "Pedrinho", LocalDate.of(2003, 5, 10), Posicao.FIXO);
        ReflectionTestUtils.setField(novo, "id", UUID.randomUUID());
        return novo;
    }

    private AtletaElenco entradaSalva(int numero, Posicao posicao) {
        AtletaElenco entrada = new AtletaElenco(inscricao, atleta, numero, posicao);
        ReflectionTestUtils.setField(entrada, "id", UUID.randomUUID());
        lenient().when(campeonatoRepository.bloquearPorEntrada(entrada.getId())).thenReturn(Optional.of(campeonato));
        lenient().when(elencoRepository.findByIdAndAtivoTrue(entrada.getId())).thenReturn(Optional.of(entrada));
        return entrada;
    }

    private ElencoResponse adicionar(UUID atletaId, int numero, Posicao posicao) {
        return elencoService.adicionar(inscricao.getId(), dirigenteId, new ElencoRequest(atletaId, numero, posicao));
    }

    private static void assertRegra(int status, String codigo, Executable acao) {
        RegraNegocioException ex = assertThrows(RegraNegocioException.class, acao);
        assertEquals(status, ex.getStatus().value());
        assertEquals(codigo, ex.getCodigo());
    }

    // Teste 11 da especificação
    @Test
    void deveAdicionarAtletaAoElenco() {
        when(elencoRepository.save(any(AtletaElenco.class))).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<AtletaElenco> captor = ArgumentCaptor.forClass(AtletaElenco.class);

        ElencoResponse resposta = adicionar(atleta.getId(), 10, Posicao.PIVO);

        verify(elencoRepository).save(captor.capture());
        AtletaElenco salva = captor.getValue();
        assertTrue(salva.isAtivo());
        assertEquals(10, salva.getNumero());
        assertEquals(Posicao.PIVO, salva.getPosicao());
        assertEquals(campeonato.getId(), salva.getCampeonatoId());
        assertEquals(inscricao.getId(), resposta.inscricaoId());
        assertEquals(atleta.getId(), resposta.atletaId());
        assertEquals(10, resposta.numero());
    }

    // Teste 12 da especificação
    @Test
    void deveLancarExcecaoQuandoNumeroEstiverForaDaFaixa() {
        assertRegra(422, "NUMERO_FORA_DA_FAIXA", () -> adicionar(atleta.getId(), 21, Posicao.FIXO));
        assertRegra(422, "NUMERO_FORA_DA_FAIXA", () -> adicionar(atleta.getId(), 0, Posicao.FIXO));
        verify(elencoRepository, never()).save(any());
    }

    // Teste 13 da especificação
    @Test
    void deveLancarExcecaoQuandoNumeroEstiverEmUso() {
        when(elencoRepository.existsByInscricaoIdAndNumeroAndAtivoTrue(inscricao.getId(), 10)).thenReturn(true);

        assertRegra(409, "NUMERO_EM_USO", () -> adicionar(atleta.getId(), 10, Posicao.FIXO));
        verify(elencoRepository, never()).save(any());
    }

    // Teste 14 da especificação
    @Test
    void deveLancarExcecaoQuandoAtletaJaEstiverNoCampeonato() {
        when(elencoRepository.existsByCampeonatoIdAndAtletaIdAndAtivoTrue(campeonato.getId(), atleta.getId()))
                .thenReturn(true);

        assertRegra(409, "ATLETA_JA_NO_CAMPEONATO", () -> adicionar(atleta.getId(), 10, Posicao.FIXO));
        verify(elencoRepository, never()).save(any());
    }

    // Teste 15 da especificação
    @Test
    void deveLancarExcecaoQuandoAtletaForDeOutroTime() {
        Time outroTime = new Time("Elétrica FC", "ELFC", Modalidade.FUTSAL, UUID.randomUUID());
        ReflectionTestUtils.setField(outroTime, "id", UUID.randomUUID());
        Atleta deOutroTime = novoAtleta(outroTime);
        when(atletaRepository.findByIdAndAtivoTrue(deOutroTime.getId())).thenReturn(Optional.of(deOutroTime));

        assertRegra(422, "ATLETA_DE_OUTRO_TIME", () -> adicionar(deOutroTime.getId(), 10, Posicao.FIXO));
        verify(elencoRepository, never()).save(any());
    }

    // Teste 16 da especificação
    @Test
    void deveLancarExcecaoQuandoInscricaoNaoEstiverAtiva() {
        for (StatusInscricao status : List.of(StatusInscricao.RECUSADA, StatusInscricao.CANCELADA,
                StatusInscricao.REMOVIDA)) {
            ReflectionTestUtils.setField(inscricao, "status", status);
            assertRegra(422, "INSCRICAO_NAO_ATIVA", () -> adicionar(atleta.getId(), 10, Posicao.FIXO));
        }
        verify(elencoRepository, never()).save(any());
    }

    // Teste 17 da especificação
    @Test
    void deveLancarExcecaoAoAlterarElencoAposInicio() {
        AtletaElenco entrada = entradaSalva(10, Posicao.FIXO);
        ReflectionTestUtils.setField(campeonato, "status", StatusCampeonato.EM_ANDAMENTO);

        assertRegra(422, "CAMPEONATO_INICIADO", () -> adicionar(atleta.getId(), 11, Posicao.FIXO));
        assertRegra(422, "CAMPEONATO_INICIADO", () -> elencoService.atualizar(entrada.getId(), dirigenteId,
                new AtualizarElencoRequest(7, Posicao.ALA)));
        assertRegra(422, "CAMPEONATO_INICIADO", () -> elencoService.remover(entrada.getId(), dirigenteId));

        assertEquals(10, entrada.getNumero());
        assertTrue(entrada.isAtivo());
    }

    // Teste 18 da especificação
    @Test
    void deveAtualizarNumeroEPosicaoNoElenco() {
        AtletaElenco entrada = entradaSalva(10, Posicao.FIXO);
        when(elencoRepository.existsByInscricaoIdAndNumeroAndAtivoTrue(inscricao.getId(), 7)).thenReturn(false);

        ElencoResponse resposta = elencoService.atualizar(entrada.getId(), dirigenteId,
                new AtualizarElencoRequest(7, Posicao.ALA));

        assertEquals(7, entrada.getNumero());
        assertEquals(Posicao.ALA, entrada.getPosicao());
        assertEquals(7, resposta.numero());
        assertEquals(Posicao.ALA, resposta.posicao());
    }

    // Teste 19 da especificação
    @Test
    void deveRemoverDoElencoMantendoHistorico() {
        AtletaElenco entrada = entradaSalva(10, Posicao.FIXO);

        elencoService.remover(entrada.getId(), dirigenteId);

        assertFalse(entrada.isAtivo());
        assertEquals(10, entrada.getNumero());
        verify(elencoRepository, never()).delete(any());
        verify(elencoRepository, never()).deleteById(any());
    }

    // Teste 20 da especificação
    @Test
    void devePermitirQueOrganizadorVejaOElenco() {
        AtletaElenco entrada = entradaSalva(10, Posicao.FIXO);
        when(elencoRepository.findByInscricaoIdAndAtivoTrueOrderByNumero(inscricao.getId()))
                .thenReturn(List.of(entrada));

        List<ElencoResponse> elenco = elencoService.listar(inscricao.getId(), organizadorId);

        assertEquals(1, elenco.size());
        assertEquals(entrada.getId(), elenco.get(0).id());
        assertRegra(403, "ACESSO_NEGADO", () -> elencoService.listar(inscricao.getId(), outroUsuarioId));
    }

    // Complementa o teste 4: só o dirigente altera o elenco, nem o organizador
    @Test
    void deveLancarExcecaoQuandoOutroUsuarioAlterarElenco() {
        AtletaElenco entrada = entradaSalva(10, Posicao.FIXO);

        assertRegra(403, "ACESSO_NEGADO", () -> elencoService.adicionar(inscricao.getId(), organizadorId,
                new ElencoRequest(atleta.getId(), 11, Posicao.FIXO)));
        assertRegra(403, "ACESSO_NEGADO", () -> elencoService.atualizar(entrada.getId(), outroUsuarioId,
                new AtualizarElencoRequest(7, Posicao.ALA)));
        assertRegra(403, "ACESSO_NEGADO", () -> elencoService.remover(entrada.getId(), outroUsuarioId));
        verify(elencoRepository, never()).existsByInscricaoIdAndNumeroAndAtivoTrue(any(), anyInt());
    }
}
