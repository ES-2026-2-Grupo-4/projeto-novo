package com.esproject4.arenaufcg.Repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.esproject4.arenaufcg.Model.Campeonato;
import com.esproject4.arenaufcg.Model.FormatoDisputa;
import com.esproject4.arenaufcg.Model.Local;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Model.StatusCampeonato;
import com.esproject4.arenaufcg.Model.TipoLocal;

@DataJpaTest
class CampeonatoRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CampeonatoRepository campeonatoRepository;

    @Autowired
    private LocalRepository localRepository;

    private UUID organizadorId;

    @BeforeEach
    void setUp() {
        organizadorId = UUID.randomUUID();
    }

    private Campeonato novoCampeonato(String nome, UUID dono) {
        return new Campeonato(nome, "Descrição", Modalidade.FUTSAL, FormatoDisputa.FASE_DE_GRUPOS, 2, 8,
            LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 12), dono);
    }

    @Test
    void devePersistirCampeonatoComTodosOsCampos() {
        Campeonato salvo = campeonatoRepository.saveAndFlush(novoCampeonato("Copa UFCG", organizadorId));
        em.clear();

        Campeonato lido = campeonatoRepository.findById(salvo.getId()).orElseThrow();
        assertEquals("Copa UFCG", lido.getNome());
        assertEquals(Modalidade.FUTSAL, lido.getModalidade());
        assertEquals(FormatoDisputa.FASE_DE_GRUPOS, lido.getFormato());
        assertEquals(2, lido.getQuantidadeGrupos());
        assertEquals(8, lido.getMaxTimes());
        assertEquals(0, lido.getVagasOcupadas());
        assertEquals(StatusCampeonato.RASCUNHO, lido.getStatus());
        assertEquals(organizadorId, lido.getOrganizadorId());
        assertTrue(lido.isAtivo());
        assertEquals(0L, lido.getVersao());
        assertNotNull(lido.getCriadoEm());
        assertNotNull(lido.getAtualizadoEm());
    }

    @Test
    void deveIncrementarVersaoAoAlterar() {
        Campeonato salvo = campeonatoRepository.saveAndFlush(novoCampeonato("Copa UFCG", organizadorId));
        salvo.reservarVaga();
        campeonatoRepository.saveAndFlush(salvo);
        em.clear();

        Campeonato lido = campeonatoRepository.findById(salvo.getId()).orElseThrow();
        assertEquals(1L, lido.getVersao());
        assertEquals(1, lido.getVagasOcupadas());
    }

    @Test
    void deveListarApenasCampeonatosAtivosDoUsuario() {
        Campeonato meu = campeonatoRepository.save(novoCampeonato("Meu", organizadorId));
        Campeonato removido = novoCampeonato("Removido", organizadorId);
        removido.remover();
        campeonatoRepository.save(removido);
        campeonatoRepository.save(novoCampeonato("De outro", UUID.randomUUID()));
        em.flush();
        em.clear();

        List<Campeonato> meus = campeonatoRepository.findAllByOrganizadorIdAndAtivoTrueOrderByCriadoEmDesc(organizadorId);

        assertEquals(1, meus.size());
        assertEquals(meu.getId(), meus.get(0).getId());
    }

    @Test
    void naoDeveEncontrarCampeonatoRemovido() {
        Campeonato campeonato = campeonatoRepository.saveAndFlush(novoCampeonato("Copa", organizadorId));
        assertTrue(campeonatoRepository.findByIdAndAtivoTrue(campeonato.getId()).isPresent());

        campeonato.remover();
        campeonatoRepository.saveAndFlush(campeonato);
        em.clear();

        assertTrue(campeonatoRepository.findByIdAndAtivoTrue(campeonato.getId()).isEmpty());
        assertTrue(campeonatoRepository.findById(campeonato.getId()).isPresent());
    }

    @Test
    void devePersistirESubstituirLocaisDoCampeonato() {
        Local ginasio = localRepository.save(new Local("Ginásio da UFCG", "Rua Aprígio Veloso, 882", TipoLocal.GINASIO));
        Local quadra = localRepository.save(new Local("Quadra do CEEI", null, TipoLocal.QUADRA));
        Local campo = localRepository.save(new Local("Campo da UFCG", null, TipoLocal.CAMPO));

        Campeonato campeonato = novoCampeonato("Copa", organizadorId);
        campeonato.definirLocais(List.of(ginasio, quadra));
        campeonato = campeonatoRepository.saveAndFlush(campeonato);
        em.clear();

        Campeonato lido = campeonatoRepository.findById(campeonato.getId()).orElseThrow();
        assertEquals(2, lido.getLocais().size());

        lido.definirLocais(List.of(localRepository.findById(campo.getId()).orElseThrow()));
        campeonatoRepository.saveAndFlush(lido);
        em.clear();

        Campeonato relido = campeonatoRepository.findById(campeonato.getId()).orElseThrow();
        assertEquals(1, relido.getLocais().size());
        assertEquals(campo.getId(), relido.getLocais().iterator().next().getId());
        // o local continua existindo e pode ser usado por outros campeonatos
        assertEquals(3, localRepository.count());
    }

    @Test
    void deveBuscarVariosLocaisPorId() {
        Local ginasio = localRepository.save(new Local("Ginásio da UFCG", null, TipoLocal.GINASIO));
        Local quadra = localRepository.save(new Local("Quadra do CEEI", null, TipoLocal.QUADRA));
        em.flush();

        List<Local> encontrados = localRepository.findAllById(List.of(ginasio.getId(), quadra.getId(), UUID.randomUUID()));

        // o service compara o tamanho com a lista pedida para responder 404 quando algum local não existe
        assertEquals(2, encontrados.size());
    }
}
