package com.esproject4.arenaufcg.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esproject4.arenaufcg.Model.PerfilPermissao;
import com.esproject4.arenaufcg.Model.UserRole;

public interface PerfilPermissaoRepository
        extends JpaRepository<PerfilPermissao, UserRole> {
}