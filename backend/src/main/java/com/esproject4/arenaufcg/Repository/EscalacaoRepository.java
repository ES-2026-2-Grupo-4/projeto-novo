package com.esproject4.arenaufcg.Repository;

import com.esproject4.arenaufcg.Model.Escalacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EscalacaoRepository extends JpaRepository<Escalacao, UUID> {

    Optional<Escalacao> findByPartidaIdAndTimeId(UUID partidaId, UUID timeId);

    boolean existsByPartidaIdAndTimeId(UUID partidaId, UUID timeId);
}
