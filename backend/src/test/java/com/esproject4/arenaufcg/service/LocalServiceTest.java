package com.esproject4.arenaufcg.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Model.Local;
import com.esproject4.arenaufcg.Model.TipoLocal;
import com.esproject4.arenaufcg.Repository.LocalRepository;

@ExtendWith(MockitoExtension.class)
class LocalServiceTest {

    @Mock LocalRepository localRepository;
    @Mock ConsultaOrganizador consultaOrganizador;

    @InjectMocks LocalService service;

    private final UUID usuarioId = UUID.randomUUID();

    @Test
    @DisplayName("organizador cadastra local → salvo")
    void cadastrarComoOrganizador() {
        when(consultaOrganizador.isOrganizador(usuarioId)).thenReturn(true);
        when(localRepository.save(any(Local.class))).thenAnswer(inv -> inv.getArgument(0));

        Local local = service.cadastrar(usuarioId, "Ginásio Central", "Rua A, 100", TipoLocal.GINASIO);

        assertThat(local.getNome()).isEqualTo("Ginásio Central");
        assertThat(local.getEndereco()).isEqualTo("Rua A, 100");
        assertThat(local.getTipo()).isEqualTo(TipoLocal.GINASIO);
        verify(localRepository).save(local);
    }

    @Test
    @DisplayName("quem não é organizador não cadastra local → 403")
    void cadastrarComoNaoOrganizador() {
        when(consultaOrganizador.isOrganizador(usuarioId)).thenReturn(false);

        var e = assertThrows(RegraNegocioException.class,
            () -> service.cadastrar(usuarioId, "Quadra", "Rua B", TipoLocal.QUADRA));

        assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(e.getCodigo()).isEqualTo("NAO_ORGANIZADOR");
        verify(localRepository, never()).save(any());
    }

    @Test
    @DisplayName("local sem nome ou sem tipo → 400")
    void cadastrarDadosInvalidos() {
        when(consultaOrganizador.isOrganizador(usuarioId)).thenReturn(true);

        var semNome = assertThrows(RegraNegocioException.class,
            () -> service.cadastrar(usuarioId, " ", "Rua", TipoLocal.CAMPO));
        var semTipo = assertThrows(RegraNegocioException.class,
            () -> service.cadastrar(usuarioId, "Campo", "Rua", null));

        assertThat(semNome.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(semTipo.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(localRepository, never()).save(any());
    }

    @Test
    @DisplayName("listar devolve todos os locais do repositório")
    void listar() {
        List<Local> locais = List.of(
            new Local("A", "Rua A", TipoLocal.CAMPO),
            new Local("B", "Rua B", TipoLocal.QUADRA));
        when(localRepository.findAll()).thenReturn(locais);

        assertThat(service.listar()).isEqualTo(locais);
    }
}
