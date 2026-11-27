package com.esproject4.arenaufcg.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esproject4.arenaufcg.Model.Escalacao;

public interface EscalacaoRepository extends JpaRepository<Escalacao, UUID> {

    Optional<Escalacao> findByPartidaIdAndTimeId(UUID partidaId, UUID timeId);

    List<Escalacao> findByPartidaId(UUID partidaId);
}
