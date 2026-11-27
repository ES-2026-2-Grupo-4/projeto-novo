package com.esproject4.arenaufcg.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.esproject4.arenaufcg.Model.MembroStaff;

public interface MembroStaffRepository extends JpaRepository<MembroStaff, UUID> {

    Optional<MembroStaff> findByIdAndAtivoTrue(UUID id);

    // Consulta explícita: o nome derivado vira "m.timeId" por causa do getTimeId(), e esse campo não existe.
    @Query("select m from MembroStaff m where m.time.id = :timeId and m.ativo = true")
    List<MembroStaff> findByTimeIdAndAtivoTrue(@Param("timeId") UUID timeId);
}