package com.esproject4.arenaufcg.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.esproject4.arenaufcg.Model.Campeonato;
import com.esproject4.arenaufcg.Model.FormatoDisputa;
import com.esproject4.arenaufcg.Model.Local;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Model.StatusCampeonato;
import com.esproject4.arenaufcg.Model.TipoLocal;
import com.esproject4.arenaufcg.Repository.CampeonatoRepository;
import com.esproject4.arenaufcg.Repository.LocalRepository;

@ExtendWith(MockitoExtension.class)
class CampeonatoServiceTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 12, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 12, 20);

    @Mock CampeonatoRepository campeonatoRepository;
    @Mock LocalRepository localRepository;
    @Mock ConsultaOrganizador consultaOrganizador;
    @Mock ConsultaInscricoes consultaInscricoes;

    @InjectMocks CampeonatoService service;

    private UUID dono;
    private UUID campeonatoId;

    @BeforeEach
    void setUp() {
        dono = UUID.randomUUID();
        campeonatoId = UUID.randomUUID();
    }


    private Campeonato novoCampeonato() {
        return new Campeonato("Copa Arena", "Descrição", Modalidade.FUTEBOL,
            FormatoDisputa.PONTOS_CORRIDOS, null, 8, INICIO, FIM, dono);
    }

    private Campeonato emAndamento() {
        Campeonato c = novoCampeonato();
        c.definirLocais(List.of(new Local("Ginásio", "Rua A", TipoLocal.GINASIO)));
        c.abrirInscricoes();
        c.encerrarInscricoes();
        c.iniciar();
        return c;
    }

    private void campeonatoExiste(Campeonato c) {
        when(campeonatoRepository.findByIdAndAtivoTrue(campeonatoId)).thenReturn(Optional.of(c));
    }

    private void salvarDevolveOArgumento() {
        when(campeonatoRepository.save(any(Campeonato.class)))
            .thenAnswer(inv -> inv.getArgument(0));
    }

    private void organizadorVerificado() {
        when(consultaOrganizador.isOrganizador(dono)).thenReturn(true);
        when(consultaOrganizador.isContaVerificada(dono)).thenReturn(true);
    }

    private Campeonato criarValido(int maxTimes, LocalDate inicio, LocalDate fim,
                                   FormatoDisputa formato, Integer grupos) {
        return service.criar(dono, "Copa", "Desc", Modalidade.FUTSAL, formato, grupos,
            maxTimes, inicio, fim);
    }

    private void assertErro(RegraNegocioException e, HttpStatus status, String codigo) {
        assertThat(e.getStatus()).isEqualTo(status);
        assertThat(e.getCodigo()).isEqualTo(codigo);
    }


    @Test
    @DisplayName("campeonato válido → salvo em RASCUNHO com o usuário como organizador")
    void criarValido() {
        organizadorVerificado();
        salvarDevolveOArgumento();

        Campeonato salvo = criarValido(8, INICIO, FIM, FormatoDisputa.MATA_MATA, null);

        assertThat(salvo.getStatus()).isEqualTo(StatusCampeonato.RASCUNHO);
        assertThat(salvo.getOrganizadorId()).isEqualTo(dono);
        assertThat(salvo.isAtivo()).isTrue();
        verify(campeonatoRepository).save(salvo);
    }

    @Test
    @DisplayName("maxTimes fora de 2 a 64 → 400 e nada é salvo")
    void criarMaxTimesInvalido() {
        organizadorVerificado();

        var baixo = assertThrows(RegraNegocioException.class,
            () -> criarValido(1, INICIO, FIM, FormatoDisputa.MATA_MATA, null));
        var alto = assertThrows(RegraNegocioException.class,
            () -> criarValido(65, INICIO, FIM, FormatoDisputa.MATA_MATA, null));

        assertErro(baixo, HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS");
        assertErro(alto, HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS");
        verify(campeonatoRepository, never()).save(any());
    }

    @Test
    @DisplayName("dataFim antes de dataInicio → 400")
    void criarDatasInvertidas() {
        organizadorVerificado();

        var e = assertThrows(RegraNegocioException.class,
            () -> criarValido(8, FIM, INICIO, FormatoDisputa.MATA_MATA, null));

        assertErro(e, HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS");
        verify(campeonatoRepository, never()).save(any());
    }

    @Test
    @DisplayName("FASE_DE_GRUPOS sem quantidadeGrupos → 400")
    void criarFaseDeGruposSemGrupos() {
        organizadorVerificado();

        var e = assertThrows(RegraNegocioException.class,
            () -> criarValido(8, INICIO, FIM, FormatoDisputa.FASE_DE_GRUPOS, null));

        assertErro(e, HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS");
        verify(campeonatoRepository, never()).save(any());
    }

    @Test
    @DisplayName("dirigente (não organizador) criando campeonato → 403")
    void criarComoDirigente() {
        when(consultaOrganizador.isOrganizador(dono)).thenReturn(false);

        var e = assertThrows(RegraNegocioException.class,
            () -> criarValido(8, INICIO, FIM, FormatoDisputa.MATA_MATA, null));

        assertErro(e, HttpStatus.FORBIDDEN, "NAO_ORGANIZADOR");
        verify(campeonatoRepository, never()).save(any());
    }

    @Test
    @DisplayName("organizador sem conta verificada → CONTA_NAO_VERIFICADA (403)")
    void criarSemContaVerificada() {
        when(consultaOrganizador.isOrganizador(dono)).thenReturn(true);
        when(consultaOrganizador.isContaVerificada(dono)).thenReturn(false);

        var e = assertThrows(RegraNegocioException.class,
            () -> criarValido(8, INICIO, FIM, FormatoDisputa.MATA_MATA, null));

        assertErro(e, HttpStatus.FORBIDDEN, "CONTA_NAO_VERIFICADA");
        verify(campeonatoRepository, never()).save(any());
    }

    @Test
    @DisplayName("listar meus → só os campeonatos ativos do usuário (consulta pelo organizador + ativo)")
    void listarMeus() {
        List<Campeonato> doUsuario = List.of(novoCampeonato(), novoCampeonato());
        when(campeonatoRepository.findAllByOrganizadorIdAndAtivoTrueOrderByCriadoEmDesc(dono))
            .thenReturn(doUsuario);

        List<Campeonato> resultado = service.listarMeus(dono);

        assertThat(resultado).isEqualTo(doUsuario);
        verify(campeonatoRepository).findAllByOrganizadorIdAndAtivoTrueOrderByCriadoEmDesc(dono);
    }

    @Test
    @DisplayName("buscar id inexistente (ou removido) → NAO_ENCONTRADO (404)")
    void buscarInexistente() {
        when(campeonatoRepository.findByIdAndAtivoTrue(campeonatoId)).thenReturn(Optional.empty());

        var e = assertThrows(RegraNegocioException.class, () -> service.buscar(campeonatoId, dono));

        assertErro(e, HttpStatus.NOT_FOUND, "NAO_ENCONTRADO");
    }

    @Test
    @DisplayName("buscar campeonato de outro usuário → ACESSO_NEGADO (403)")
    void buscarDeOutroUsuario() {
        campeonatoExiste(novoCampeonato());

        var e = assertThrows(RegraNegocioException.class,
            () -> service.buscar(campeonatoId, UUID.randomUUID()));

        assertErro(e, HttpStatus.FORBIDDEN, "ACESSO_NEGADO");
    }

    @Test
    @DisplayName("buscar pelo dono devolve o campeonato")
    void buscarPeloDono() {
        Campeonato c = novoCampeonato();
        campeonatoExiste(c);

        assertThat(service.buscar(campeonatoId, dono)).isSameAs(c);
    }

    @Test
    @DisplayName("dono atualiza em RASCUNHO → alterações salvas")
    void atualizarPeloDono() {
        Campeonato c = novoCampeonato();
        campeonatoExiste(c);
        salvarDevolveOArgumento();

        Campeonato r = service.atualizar(campeonatoId, dono, "Novo", "Nova", Modalidade.VOLEI,
            FormatoDisputa.MATA_MATA, null, 16, INICIO, FIM);

        assertThat(r.getNome()).isEqualTo("Novo");
        assertThat(r.getModalidade()).isEqualTo(Modalidade.VOLEI);
        verify(campeonatoRepository).save(c);
    }

    @Test
    @DisplayName("outro usuário editando → ACESSO_NEGADO e nada é salvo")
    void atualizarPorOutroUsuario() {
        campeonatoExiste(novoCampeonato());

        var e = assertThrows(RegraNegocioException.class, () ->
            service.atualizar(campeonatoId, UUID.randomUUID(), "Novo", "Nova", Modalidade.FUTEBOL,
                FormatoDisputa.PONTOS_CORRIDOS, null, 8, INICIO, FIM));

        assertErro(e, HttpStatus.FORBIDDEN, "ACESSO_NEGADO");
        verify(campeonatoRepository, never()).save(any());
    }

    @Test
    @DisplayName("mudar modalidade com inscrições abertas pelo service → CAMPO_BLOQUEADO")
    void atualizarModalidadeComInscricoesAbertas() {
        Campeonato c = novoCampeonato();
        c.definirLocais(List.of(new Local("Ginásio", "Rua A", TipoLocal.GINASIO)));
        c.abrirInscricoes();
        campeonatoExiste(c);

        var e = assertThrows(RegraNegocioException.class, () ->
            service.atualizar(campeonatoId, dono, "Copa Arena", "Descrição", Modalidade.BASQUETE,
                FormatoDisputa.PONTOS_CORRIDOS, null, 8, INICIO, FIM));

        assertErro(e, HttpStatus.UNPROCESSABLE_CONTENT, "CAMPO_BLOQUEADO");
        verify(campeonatoRepository, never()).save(any());
    }


    @Test
    @DisplayName("remover → fica inativo (some da listagem) e é salvo, não apagado do banco")
    void removerFazRemocaoLogica() {
        Campeonato c = novoCampeonato();
        campeonatoExiste(c);
        when(consultaInscricoes.possuiInscricoesAtivas(campeonatoId)).thenReturn(false);

        service.remover(campeonatoId, dono);

        assertThat(c.isAtivo()).isFalse();
        verify(campeonatoRepository).save(c);
        verify(campeonatoRepository, never()).delete(any());
        verify(campeonatoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("outro usuário removendo → ACESSO_NEGADO")
    void removerPorOutroUsuario() {
        Campeonato c = novoCampeonato();
        campeonatoExiste(c);

        var e = assertThrows(RegraNegocioException.class,
            () -> service.remover(campeonatoId, UUID.randomUUID()));

        assertErro(e, HttpStatus.FORBIDDEN, "ACESSO_NEGADO");
        assertThat(c.isAtivo()).isTrue();
        verify(campeonatoRepository, never()).save(any());
    }

    @Test
    @DisplayName("remover depois do início → CAMPEONATO_INICIADO (422)")
    void removerDepoisDoInicio() {
        Campeonato c = emAndamento();
        campeonatoExiste(c);

        var e = assertThrows(RegraNegocioException.class,
            () -> service.remover(campeonatoId, dono));

        assertErro(e, HttpStatus.UNPROCESSABLE_CONTENT, "CAMPEONATO_INICIADO");
        assertThat(c.isAtivo()).isTrue();
        verify(campeonatoRepository, never()).save(any());
    }

    @Test
    @DisplayName("remover com inscrição pendente/aprovada → 409")
    void removerComInscricoesAtivas() {
        Campeonato c = novoCampeonato();
        campeonatoExiste(c);
        when(consultaInscricoes.possuiInscricoesAtivas(campeonatoId)).thenReturn(true);

        var e = assertThrows(RegraNegocioException.class,
            () -> service.remover(campeonatoId, dono));

        assertErro(e, HttpStatus.CONFLICT, "INSCRICOES_ATIVAS");
        assertThat(c.isAtivo()).isTrue();
        verify(campeonatoRepository, never()).save(any());
    }


    @Test
    @DisplayName("abrir inscrições sem local → 422")
    void abrirInscricoesSemLocal() {
        campeonatoExiste(novoCampeonato());

        var e = assertThrows(RegraNegocioException.class,
            () -> service.abrirInscricoes(campeonatoId, dono));

        assertErro(e, HttpStatus.UNPROCESSABLE_CONTENT, "SEM_LOCAL");
        verify(campeonatoRepository, never()).save(any());
    }

    @Test
    @DisplayName("abrir inscrições com local → INSCRICOES_ABERTAS")
    void abrirInscricoesComLocal() {
        Campeonato c = novoCampeonato();
        c.definirLocais(List.of(new Local("Ginásio", "Rua A", TipoLocal.GINASIO)));
        campeonatoExiste(c);
        salvarDevolveOArgumento();

        Campeonato r = service.abrirInscricoes(campeonatoId, dono);

        assertThat(r.getStatus()).isEqualTo(StatusCampeonato.INSCRICOES_ABERTAS);
        verify(campeonatoRepository).save(c);
    }

    @Test
    @DisplayName("encerrar inscrições em RASCUNHO → TRANSICAO_INVALIDA (409)")
    void encerrarInscricoesEmRascunho() {
        campeonatoExiste(novoCampeonato());

        var e = assertThrows(RegraNegocioException.class,
            () -> service.encerrarInscricoes(campeonatoId, dono));

        assertErro(e, HttpStatus.CONFLICT, "TRANSICAO_INVALIDA");
        verify(campeonatoRepository, never()).save(any());
    }

    @Test
    @DisplayName("abrir e encerrar inscrições por outro usuário → ACESSO_NEGADO")
    void abrirEEncerrarPorOutroUsuario() {
        campeonatoExiste(novoCampeonato());
        UUID intruso = UUID.randomUUID();

        var abrir = assertThrows(RegraNegocioException.class,
            () -> service.abrirInscricoes(campeonatoId, intruso));
        var encerrar = assertThrows(RegraNegocioException.class,
            () -> service.encerrarInscricoes(campeonatoId, intruso));

        assertErro(abrir, HttpStatus.FORBIDDEN, "ACESSO_NEGADO");
        assertErro(encerrar, HttpStatus.FORBIDDEN, "ACESSO_NEGADO");
    }


    @Test
    @DisplayName("definir locais → a nova lista substitui a anterior")
    void definirLocaisSubstituiAAnterior() {
        Campeonato c = novoCampeonato();
        Local antigo = new Local("Antigo", "Rua A", TipoLocal.CAMPO);
        c.definirLocais(List.of(antigo));
        campeonatoExiste(c);
        salvarDevolveOArgumento();

        Local novo = new Local("Novo", "Rua B", TipoLocal.QUADRA);
        UUID novoId = UUID.randomUUID();
        when(localRepository.findAllById(any())).thenReturn(List.of(novo));

        Campeonato r = service.definirLocais(campeonatoId, dono, Arrays.asList(novoId));

        assertThat(r.getLocais()).containsExactly(novo).doesNotContain(antigo);
        verify(campeonatoRepository).save(c);
    }

    @Test
    @DisplayName("local inexistente → NAO_ENCONTRADO (404)")
    void definirLocaisComLocalInexistente() {
        campeonatoExiste(novoCampeonato());
        UUID existente = UUID.randomUUID();
        UUID inexistente = UUID.randomUUID();
        when(localRepository.findAllById(any()))
            .thenReturn(List.of(new Local("Só este", "Rua A", TipoLocal.CAMPO)));

        var e = assertThrows(RegraNegocioException.class,
            () -> service.definirLocais(campeonatoId, dono, Arrays.asList(existente, inexistente)));


        assertErro(e, HttpStatus.NOT_FOUND, "NAO_ENCONTRADO");
        verify(campeonatoRepository, never()).save(any());
    }

    @Test
    @DisplayName("definir locais depois do início → CAMPEONATO_INICIADO (422)")
    void definirLocaisDepoisDoInicio() {
        campeonatoExiste(emAndamento());
        UUID id = UUID.randomUUID();
        when(localRepository.findAllById(any()))
            .thenReturn(List.of(new Local("Outro", "Rua X", TipoLocal.CAMPO)));

        var e = assertThrows(RegraNegocioException.class,
            () -> service.definirLocais(campeonatoId, dono, Arrays.asList(id)));

        assertErro(e, HttpStatus.UNPROCESSABLE_CONTENT, "CAMPEONATO_INICIADO");
        verify(campeonatoRepository, never()).save(any());
    }

    @Test
    @DisplayName("definir locais por outro usuário → ACESSO_NEGADO")
    void definirLocaisPorOutroUsuario() {
        campeonatoExiste(novoCampeonato());

        var e = assertThrows(RegraNegocioException.class,
            () -> service.definirLocais(campeonatoId, UUID.randomUUID(), List.of(UUID.randomUUID())));

        assertErro(e, HttpStatus.FORBIDDEN, "ACESSO_NEGADO");
    }
}