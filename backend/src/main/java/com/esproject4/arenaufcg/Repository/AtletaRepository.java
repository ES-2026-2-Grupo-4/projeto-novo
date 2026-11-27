package com.esproject4.arenaufcg.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.esproject4.arenaufcg.Model.Atleta;

public interface AtletaRepository extends JpaRepository<Atleta, UUID> {

    Optional<Atleta> findByIdAndAtivoTrue(UUID id);

    // Consulta explícita: o nome derivado vira "a.timeId" por causa do getTimeId(), e esse campo não existe.
    @Query("select a from Atleta a where a.time.id = :timeId and a.ativo = true")
    List<Atleta> findByTimeIdAndAtivoTrue(@Param("timeId") UUID timeId);
}