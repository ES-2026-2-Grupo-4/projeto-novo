package com.esproject4.arenaufcg.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.Campeonato;
import com.esproject4.arenaufcg.Model.Local;
import com.esproject4.arenaufcg.Model.Partida;
import com.esproject4.arenaufcg.Repository.PartidaRepository;

// Testes da HU-16 (definir o local de mando). A Partida é mockada: a regra de
// editabilidade (só AGENDADA) é dela e vive no Partida.definirLocal — aqui testamos
// a orquestração do serviço (busca, permissão, validação do local e persistência).
@ExtendWith(MockitoExtension.class)
class MandoServiceTest {

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private PermissaoPartida permissaoPartida;

    @InjectMocks
    private MandoService mandoService;

    private UUID usuarioId;
    private UUID partidaId;
    private UUID localId;
    private Partida partida;
    private Campeonato campeonato;
    private Local local;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        partidaId = UUID.randomUUID();
        localId = UUID.randomUUID();
        partida = org.mockito.Mockito.mock(Partida.class);
        campeonato = org.mockito.Mockito.mock(Campeonato.class);
        local = org.mockito.Mockito.mock(Local.class);
    }

    @Test
    void deveDefinirLocalComSucesso() {
        when(partidaRepository.findById(partidaId)).thenReturn(Optional.of(partida));
        when(partida.getCampeonato()).thenReturn(campeonato);
        when(campeonato.getLocais()).thenReturn(Set.of(local));
        when(local.getId()).thenReturn(localId);
        when(partidaRepository.save(partida)).thenReturn(partida);

        Partida resultado = mandoService.definirLocal(usuarioId, partidaId, localId);

        assertThat(resultado).isSameAs(partida);
        verify(permissaoPartida).exigirMandante(partida, usuarioId);
        verify(partida).definirLocal(localId);
        verify(partidaRepository).save(partida);
    }

    @Test
    void deveLancar404QuandoPartidaNaoExiste() {
        when(partidaRepository.findById(partidaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mandoService.definirLocal(usuarioId, partidaId, localId))
                .isInstanceOf(RegraNegocioException.class)
                .satisfies(ex -> {
                    RegraNegocioException rne = (RegraNegocioException) ex;
                    assertThat(rne.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(rne.getCodigo()).isEqualTo("PARTIDA_NAO_ENCONTRADA");
                });

        verify(partidaRepository, never()).save(any());
    }

    @Test
    void deveLancar403QuandoNaoEhMandante() {
        when(partidaRepository.findById(partidaId)).thenReturn(Optional.of(partida));
        org.mockito.Mockito.doThrow(new RegraNegocioException(
                        HttpStatus.FORBIDDEN, "ACESSO_NEGADO", "Acesso negado."))
                .when(permissaoPartida).exigirMandante(partida, usuarioId);

        assertThatThrownBy(() -> mandoService.definirLocal(usuarioId, partidaId, localId))
                .isInstanceOf(RegraNegocioException.class)
                .satisfies(ex -> assertThat(((RegraNegocioException) ex).getStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verify(partida, never()).definirLocal(any());
        verify(partidaRepository, never()).save(any());
    }

    @Test
    void deveLancarLocalInvalidoQuandoLocalNaoPertenceAoCampeonato() {
        when(partidaRepository.findById(partidaId)).thenReturn(Optional.of(partida));
        when(partida.getCampeonato()).thenReturn(campeonato);
        when(campeonato.getLocais()).thenReturn(Set.of(local));
        when(local.getId()).thenReturn(UUID.randomUUID()); // outro local, não o pedido

        assertThatThrownBy(() -> mandoService.definirLocal(usuarioId, partidaId, localId))
                .isInstanceOf(RegraNegocioException.class)
                .satisfies(ex -> {
                    RegraNegocioException rne = (RegraNegocioException) ex;
                    assertThat(rne.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
                    assertThat(rne.getCodigo()).isEqualTo("LOCAL_INVALIDO");
                });

        verify(partida, never()).definirLocal(any());
        verify(partidaRepository, never()).save(any());
    }

    @Test
    void deveLancarLocalInvalidoQuandoCampeonatoNaoTemLocais() {
        when(partidaRepository.findById(partidaId)).thenReturn(Optional.of(partida));
        when(partida.getCampeonato()).thenReturn(campeonato);
        when(campeonato.getLocais()).thenReturn(Set.of());

        assertThatThrownBy(() -> mandoService.definirLocal(usuarioId, partidaId, localId))
                .isInstanceOf(RegraNegocioException.class)
                .satisfies(ex -> assertThat(((RegraNegocioException) ex).getCodigo())
                        .isEqualTo("LOCAL_INVALIDO"));

        verify(partidaRepository, never()).save(any());
    }

    @Test
    void devePropagarPartidaNaoEditavelVindaDaPartida() {
        // Quando a partida não está AGENDADA, o Partida.definirLocal lança 409.
        when(partidaRepository.findById(partidaId)).thenReturn(Optional.of(partida));
        when(partida.getCampeonato()).thenReturn(campeonato);
        when(campeonato.getLocais()).thenReturn(Set.of(local));
        when(local.getId()).thenReturn(localId);
        org.mockito.Mockito.doThrow(new RegraNegocioException(
                        HttpStatus.CONFLICT, "PARTIDA_NAO_EDITAVEL", "Não editável."))
                .when(partida).definirLocal(localId);

        assertThatThrownBy(() -> mandoService.definirLocal(usuarioId, partidaId, localId))
                .isInstanceOf(RegraNegocioException.class)
                .satisfies(ex -> {
                    RegraNegocioException rne = (RegraNegocioException) ex;
                    assertThat(rne.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(rne.getCodigo()).isEqualTo("PARTIDA_NAO_EDITAVEL");
                });

        verify(partidaRepository, never()).save(any());
    }

    @Test
    void deveValidarPermissaoAntesDeValidarLocal() {
        // Garante a ordem: nem toca no campeonato/local se o usuário não é o mandante.
        when(partidaRepository.findById(partidaId)).thenReturn(Optional.of(partida));
        org.mockito.Mockito.doThrow(new RegraNegocioException(
                        HttpStatus.FORBIDDEN, "ACESSO_NEGADO", "Acesso negado."))
                .when(permissaoPartida).exigirMandante(partida, usuarioId);

        assertThatThrownBy(() -> mandoService.definirLocal(usuarioId, partidaId, localId))
                .isInstanceOf(RegraNegocioException.class);

        verify(partida, never()).getCampeonato();
    }
}
