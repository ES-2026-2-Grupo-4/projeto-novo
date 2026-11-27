package com.esproject4.arenaufcg.Service;

import com.esproject4.arenaufcg.DTO.AtletaRequest;
import com.esproject4.arenaufcg.DTO.AtletaResponse;
import com.esproject4.arenaufcg.Model.Atleta;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Model.Posicao;
import com.esproject4.arenaufcg.Model.Time;
import com.esproject4.arenaufcg.Repository.AtletaElencoRepository;
import com.esproject4.arenaufcg.Repository.AtletaRepository;
import com.esproject4.arenaufcg.Repository.TimeRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtletaServiceTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @Mock
    private AtletaRepository atletaRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private AtletaElencoRepository elencoRepository;

    @InjectMocks
    private AtletaService atletaService;

    private UUID dirigenteId;
    private UUID outroUsuarioId;
    private UUID timeId;
    private Time time;

    @BeforeAll
    static void criarValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void fecharValidator() {
        validatorFactory.close();
    }

    @BeforeEach
    void setUp() {
        dirigenteId = UUID.randomUUID();
        outroUsuarioId = UUID.randomUUID();
        timeId = UUID.randomUUID();
        time = new Time("Computação FC", "CCFC", Modalidade.FUTSAL, dirigenteId);
        ReflectionTestUtils.setField(time, "id", timeId);
    }

    private static AtletaRequest request(String nome, LocalDate nascimento, Posicao posicao) {
        return new AtletaRequest(nome, "Apelido", nascimento, posicao);
    }

    private Atleta atletaSalvo() {
        Atleta atleta = new Atleta(time, "Pedro Silva", "Pedrinho", LocalDate.of(2003, 5, 10), Posicao.FIXO);
        ReflectionTestUtils.setField(atleta, "id", UUID.randomUUID());
        return atleta;
    }

    private static void assertRegra(int status, String codigo, Executable acao) {
        com.esproject4.arenaufcg.Exception.RegraNegocioException ex =
                assertThrows(com.esproject4.arenaufcg.Exception.RegraNegocioException.class, acao);
        assertEquals(status, ex.getStatus().value());
        assertEquals(codigo, ex.getCodigo());
    }

    private static void assertRegraService(int status, String codigo, Executable acao) {
        RegraNegocioException ex = assertThrows(RegraNegocioException.class, acao);
        assertEquals(status, ex.getStatus().value());
        assertEquals(codigo, ex.getCodigo());
    }

    // Teste 1 da especificação
    @Test
    void deveCadastrarAtletaAtivoNoTimeDoDirigente() {
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(atletaRepository.save(any(Atleta.class))).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<Atleta> captor = ArgumentCaptor.forClass(Atleta.class);

        AtletaResponse resposta = atletaService.cadastrar(timeId, dirigenteId,
                request("Pedro Silva", LocalDate.of(2003, 5, 10), Posicao.GOLEIRO));

        verify(atletaRepository).save(captor.capture());
        assertTrue(captor.getValue().isAtivo());
        assertEquals(timeId, captor.getValue().getTimeId());
        assertEquals("Pedro Silva", resposta.nome());
        assertEquals(Posicao.GOLEIRO, resposta.posicaoPreferida());
        assertTrue(resposta.ativo());
    }

    // Teste 2 da especificação (nome vazio)
    @Test
    void deveRecusarAtletaComNomeVazio() {
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));

        assertRegra(400, "DADOS_INVALIDOS", () -> atletaService.cadastrar(timeId, dirigenteId,
                request("   ", LocalDate.of(2003, 5, 10), null)));
        verify(atletaRepository, never()).save(any());
    }

    // Teste 2 da especificação (data de nascimento no futuro, barrada pelo @Past do DTO)
    @Test
    void deveRecusarDataDeNascimentoNoFuturo() {
        var violacoes = validator.validate(request("Pedro Silva", LocalDate.now().plusDays(1), null));

        assertEquals(1, violacoes.size());
        assertEquals("dataNascimento", violacoes.iterator().next().getPropertyPath().toString());
    }

    // Teste 3 da especificação
    @Test
    void deveRecusarPosicaoPreferidaDeOutraModalidade() {
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));

        assertRegraService(422, "POSICAO_INVALIDA", () -> atletaService.cadastrar(timeId, dirigenteId,
                request("Pedro Silva", LocalDate.of(2003, 5, 10), Posicao.LEVANTADOR)));
        verify(atletaRepository, never()).save(any());
    }

    // Teste 4 da especificação (atletas)
    @Test
    void outroUsuarioNaoPodeCadastrarAtualizarNemRemoverAtleta() {
        Atleta atleta = atletaSalvo();
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(atletaRepository.findByIdAndAtivoTrue(atleta.getId())).thenReturn(Optional.of(atleta));
        AtletaRequest dados = request("Outro Nome", LocalDate.of(2003, 5, 10), null);

        assertRegraService(403, "ACESSO_NEGADO", () -> atletaService.cadastrar(timeId, outroUsuarioId, dados));
        assertRegraService(403, "ACESSO_NEGADO",
                () -> atletaService.atualizar(atleta.getId(), outroUsuarioId, dados));
        assertRegraService(403, "ACESSO_NEGADO", () -> atletaService.remover(atleta.getId(), outroUsuarioId));

        verify(atletaRepository, never()).save(any());
        assertEquals("Pedro Silva", atleta.getNome());
        assertTrue(atleta.isAtivo());
    }

    // Teste 5 da especificação
    @Test
    void deveListarApenasAtletasAtivosDoTime() {
        Atleta atleta = atletaSalvo();
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(atletaRepository.findByTimeIdAndAtivoTrue(timeId)).thenReturn(List.of(atleta));

        List<AtletaResponse> lista = atletaService.listarDoTime(timeId, dirigenteId);

        assertEquals(1, lista.size());
        assertEquals(atleta.getId(), lista.get(0).id());
        assertEquals(timeId, lista.get(0).timeId());
    }

    // Teste 6 da especificação
    @Test
    void removerAtletaDesativaSemApagarDoBanco() {
        Atleta atleta = atletaSalvo();
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(atletaRepository.findByIdAndAtivoTrue(atleta.getId())).thenReturn(Optional.of(atleta));
        when(elencoRepository.atletaEmElencoAtivoNaoFinalizado(atleta.getId())).thenReturn(false);

        atletaService.remover(atleta.getId(), dirigenteId);

        assertFalse(atleta.isAtivo());
        verify(atletaRepository, never()).delete(any());
        verify(atletaRepository, never()).deleteById(any());
    }

    // Teste 7 da especificação
    @Test
    void naoRemoveAtletaEmElencoDeCampeonatoNaoFinalizado() {
        Atleta atleta = atletaSalvo();
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(atletaRepository.findByIdAndAtivoTrue(atleta.getId())).thenReturn(Optional.of(atleta));
        when(elencoRepository.atletaEmElencoAtivoNaoFinalizado(atleta.getId())).thenReturn(true);

        assertRegraService(409, "ATLETA_EM_ELENCO_ATIVO", () -> atletaService.remover(atleta.getId(), dirigenteId));
        assertTrue(atleta.isAtivo());
    }
}
