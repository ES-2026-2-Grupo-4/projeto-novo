package com.esproject4.arenaufcg.Repository;

import com.esproject4.arenaufcg.Model.Time;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TimeRepository extends JpaRepository<Time, UUID> {

    List<Time> findByDirigenteIdAndAtivoTrue(UUID dirigenteId);
}
