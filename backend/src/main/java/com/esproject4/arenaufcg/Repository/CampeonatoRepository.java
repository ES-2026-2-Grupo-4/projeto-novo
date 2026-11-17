package com.esproject4.arenaufcg.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esproject4.arenaufcg.Model.Campeonato;

public interface CampeonatoRepository extends JpaRepository<Campeonato, UUID> {

    /** Campeonato ativo (não removido). Removido conta como inexistente (404). */
    Optional<Campeonato> findByIdAndAtivoTrue(UUID id);

    /** "Meus campeonatos": só os ativos do organizador, mais recentes primeiro. */
    List<Campeonato> findAllByOrganizadorIdAndAtivoTrueOrderByCriadoEmDesc(UUID organizadorId);

    List<Campeonato> findByOrganizadorIdAndAtivoTrue(UUID organizadorId);
}
