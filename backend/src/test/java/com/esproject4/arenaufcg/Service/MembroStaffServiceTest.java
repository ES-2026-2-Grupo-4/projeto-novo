package com.esproject4.arenaufcg.Service;

import com.esproject4.arenaufcg.DTO.MembroStaffRequest;
import com.esproject4.arenaufcg.DTO.MembroStaffResponse;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.FuncaoStaff;
import com.esproject4.arenaufcg.Model.MembroStaff;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Model.Time;
import com.esproject4.arenaufcg.Repository.MembroStaffRepository;
import com.esproject4.arenaufcg.Repository.TimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
class MembroStaffServiceTest {

    @Mock
    private MembroStaffRepository membroStaffRepository;

    @Mock
    private TimeRepository timeRepository;

    @InjectMocks
    private MembroStaffService membroStaffService;

    private UUID dirigenteId;
    private UUID outroUsuarioId;
    private UUID timeId;
    private Time time;

    @BeforeEach
    void setUp() {
        dirigenteId = UUID.randomUUID();
        outroUsuarioId = UUID.randomUUID();
        timeId = UUID.randomUUID();
        time = new Time("Computação FC", "CCFC", Modalidade.FUTSAL, dirigenteId);
        ReflectionTestUtils.setField(time, "id", timeId);
    }

    private MembroStaff membroSalvo() {
        MembroStaff membro = new MembroStaff(time, "Carlos Souza", FuncaoStaff.TECNICO);
        ReflectionTestUtils.setField(membro, "id", UUID.randomUUID());
        return membro;
    }

    private static void assertRegra(int status, String codigo, Executable acao) {
        RegraNegocioException ex = assertThrows(RegraNegocioException.class, acao);
        assertEquals(status, ex.getStatus().value());
        assertEquals(codigo, ex.getCodigo());
    }

    // Teste 8 da especificação (cadastro)
    @Test
    void deveCadastrarMembroDoStaffAtivoNoTimeDoDirigente() {
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(membroStaffRepository.save(any(MembroStaff.class))).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<MembroStaff> captor = ArgumentCaptor.forClass(MembroStaff.class);

        MembroStaffResponse resposta = membroStaffService.cadastrar(timeId, dirigenteId,
                new MembroStaffRequest("Carlos Souza", FuncaoStaff.TECNICO));

        verify(membroStaffRepository).save(captor.capture());
        assertTrue(captor.getValue().isAtivo());
        assertEquals(timeId, captor.getValue().getTimeId());
        assertEquals("Carlos Souza", resposta.nome());
        assertEquals(FuncaoStaff.TECNICO, resposta.funcao());
    }

    // Teste 8 da especificação (remoção)
    @Test
    void removerMembroDoStaffDesativaSemApagarDoBanco() {
        MembroStaff membro = membroSalvo();
        when(membroStaffRepository.findByIdAndAtivoTrue(membro.getId())).thenReturn(Optional.of(membro));

        membroStaffService.remover(membro.getId(), dirigenteId);

        assertFalse(membro.isAtivo());
        verify(membroStaffRepository, never()).delete(any());
        verify(membroStaffRepository, never()).deleteById(any());
    }

    // Teste 8 da especificação (listagem só com os ativos)
    @Test
    void deveListarApenasMembrosAtivosDoTime() {
        MembroStaff membro = membroSalvo();
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(membroStaffRepository.findByTimeIdAndAtivoTrue(timeId)).thenReturn(List.of(membro));

        List<MembroStaffResponse> lista = membroStaffService.listarDoTime(timeId, dirigenteId);

        assertEquals(1, lista.size());
        assertEquals(membro.getId(), lista.get(0).id());
    }

    // Teste 4 da especificação (staff)
    @Test
    void outroUsuarioNaoPodeCadastrarAtualizarNemRemoverStaff() {
        MembroStaff membro = membroSalvo();
        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(membroStaffRepository.findByIdAndAtivoTrue(membro.getId())).thenReturn(Optional.of(membro));
        MembroStaffRequest dados = new MembroStaffRequest("Outro Nome", FuncaoStaff.MEDICO);

        assertRegra(403, "ACESSO_NEGADO", () -> membroStaffService.cadastrar(timeId, outroUsuarioId, dados));
        assertRegra(403, "ACESSO_NEGADO", () -> membroStaffService.atualizar(membro.getId(), outroUsuarioId, dados));
        assertRegra(403, "ACESSO_NEGADO", () -> membroStaffService.remover(membro.getId(), outroUsuarioId));

        verify(membroStaffRepository, never()).save(any());
        assertEquals("Carlos Souza", membro.getNome());
        assertTrue(membro.isAtivo());
    }
}
