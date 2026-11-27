package com.esproject4.arenaufcg.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.esproject4.arenaufcg.Model.Campeonato;

public interface CampeonatoRepository extends JpaRepository<Campeonato, UUID> {

    /** Campeonato ativo (não removido). Removido conta como inexistente (404). */
    Optional<Campeonato> findByIdAndAtivoTrue(UUID id);

    /** "Meus campeonatos": só os ativos do organizador, mais recentes primeiro. */
    List<Campeonato> findAllByOrganizadorIdAndAtivoTrueOrderByCriadoEmDesc(UUID organizadorId);

    // Serializa alterações do elenco inclusive entre inscrições do mesmo campeonato.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Campeonato c where c.id = (select i.campeonato.id from Inscricao i where i.id = :id)")
    Optional<Campeonato> bloquearPorInscricao(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Campeonato c where c.id = (select e.campeonatoId from AtletaElenco e where e.id = :id)")
    Optional<Campeonato> bloquearPorEntrada(@Param("id") UUID id);
}
