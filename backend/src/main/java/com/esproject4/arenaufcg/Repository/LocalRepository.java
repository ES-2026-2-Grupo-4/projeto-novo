package com.esproject4.arenaufcg.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esproject4.arenaufcg.Model.Local;

public interface LocalRepository extends JpaRepository<Local, UUID> {}
