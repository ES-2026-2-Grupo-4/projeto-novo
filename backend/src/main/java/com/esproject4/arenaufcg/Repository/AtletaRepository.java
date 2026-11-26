package com.esproject4.arenaufcg.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esproject4.arenaufcg.Model.Atleta;

public interface AtletaRepository extends JpaRepository<Atleta, UUID> {

    Optional<Atleta> findByIdAndAtivoTrue(UUID id);

    List<Atleta> findByTimeIdAndAtivoTrue(UUID timeId);
}