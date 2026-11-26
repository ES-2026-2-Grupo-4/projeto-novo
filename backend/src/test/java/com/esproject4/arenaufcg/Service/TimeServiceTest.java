package com.esproject4.arenaufcg.Service;

import com.esproject4.arenaufcg.DTO.CriarTimeRequest;
import com.esproject4.arenaufcg.DTO.EditarTimeRequest;
import com.esproject4.arenaufcg.DTO.TimeResponse;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Model.Time;
import com.esproject4.arenaufcg.Repository.TimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeServiceTest {

    @Mock
    private TimeRepository timeRepository;

    @InjectMocks
    private TimeService timeService;

    private UUID dirigenteId;
    private UUID outroUsuarioId;

    @BeforeEach
    void setUp() {
        dirigenteId = UUID.randomUUID();
        outroUsuarioId = UUID.randomUUID();
    }

    // Teste 1 da especificação
    @Test
    void deveCadastrarTimeComSucesso() {
        when(timeRepository.save(any(Time.class))).thenAnswer(inv -> inv.getArgument(0));

        TimeResponse resposta = timeService.criar(
                new CriarTimeRequest("Computação FC", "CCFC", Modalidade.FUTSAL), dirigenteId);

        assertEquals("Computação FC", resposta.nome());
        assertEquals("CCFC", resposta.sigla());
        assertEquals(Modalidade.FUTSAL, resposta.modalidade());
    }

    // Teste 3 da especificação
    @Test
    void deveDefinirDirigenteComoUsuarioAutenticadoNoCadastro() {
        when(timeRepository.save(any(Time.class))).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<Time> captor = ArgumentCaptor.forClass(Time.class);

        timeService.criar(new CriarTimeRequest("Computação FC", "CCFC", Modalidade.FUTSAL), dirigenteId);

        verify(timeRepository).save(captor.capture());
        assertEquals(dirigenteId, captor.getValue().getDirigenteId());
    }

    @Test
    void deveListarApenasOsTimesDoDirigente() {
        Time time = new Time("Computação FC", "CCFC", Modalidade.FUTSAL, dirigenteId);
        when(timeRepository.findByDirigenteIdAndAtivoTrue(dirigenteId)).thenReturn(List.of(time));

        List<TimeResponse> times = timeService.meusTimes(dirigenteId);

        assertEquals(1, times.size());
        assertEquals("CCFC", times.get(0).sigla());
    }

    // Teste 4 da especificação
    @Test
    void deveEditarTimeQuandoUsuarioForDono() {
        UUID teamId = UUID.randomUUID();
        Time time = new Time("Computação FC", "CCFC", Modalidade.FUTSAL, dirigenteId);
        when(timeRepository.findById(teamId)).thenReturn(Optional.of(time));
        when(timeRepository.save(any(Time.class))).thenAnswer(inv -> inv.getArgument(0));

        TimeResponse resposta = timeService.editar(
                teamId, new EditarTimeRequest("Computação Futsal Clube", "CFC"), dirigenteId);

        assertEquals("Computação Futsal Clube", resposta.nome());
        assertEquals("CFC", resposta.sigla());
        assertEquals(Modalidade.FUTSAL, resposta.modalidade());
    }

    // Teste 5 da especificação
    @Test
    void deveLancarExcecaoQuandoUsuarioNaoForDonoDoTime() {
        UUID teamId = UUID.randomUUID();
        Time time = new Time("Computação FC", "CCFC", Modalidade.FUTSAL, dirigenteId);
        when(timeRepository.findById(teamId)).thenReturn(Optional.of(time));

        assertThrows(RegraNegocioException.class, () ->
                timeService.editar(teamId, new EditarTimeRequest("Outro", "OUT"), outroUsuarioId));

        verify(timeRepository, never()).save(any(Time.class));
    }

    @Test
    void deveLancarExcecaoQuandoTimeNaoExistir() {
        UUID teamId = UUID.randomUUID();
        when(timeRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () ->
                timeService.editar(teamId, new EditarTimeRequest("Outro", "OUT"), dirigenteId));
    }
}
