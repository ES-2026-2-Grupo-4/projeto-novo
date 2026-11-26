package com.esproject4.arenaufcg.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esproject4.arenaufcg.Model.MembroStaff;

public interface MembroStaffRepository extends JpaRepository<MembroStaff, UUID> {

    Optional<MembroStaff> findByIdAndAtivoTrue(UUID id);

    List<MembroStaff> findByTimeIdAndAtivoTrue(UUID timeId);
}