package com.esproject4.arenaufcg.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.esproject4.arenaufcg.Model.LogAuditoria;

public interface LogAuditoriaRepository extends
        JpaRepository<LogAuditoria, UUID>,
        JpaSpecificationExecutor<LogAuditoria> {
}