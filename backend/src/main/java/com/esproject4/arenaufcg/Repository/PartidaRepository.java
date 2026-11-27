package com.esproject4.arenaufcg.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esproject4.arenaufcg.Model.Partida;

// PROVISÓRIO (T08): sai quando a T07 entregar o repositório oficial da Partida.
public interface PartidaRepository extends JpaRepository<Partida, UUID> {
}
