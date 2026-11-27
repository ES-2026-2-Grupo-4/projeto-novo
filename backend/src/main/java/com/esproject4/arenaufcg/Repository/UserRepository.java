package com.esproject4.arenaufcg.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.esproject4.arenaufcg.Model.User;
import com.esproject4.arenaufcg.Model.UserRole;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;



public interface UserRepository extends JpaRepository<User, UUID> {

    // email.address: o Spring Data navega pelo @Embedded Email até o campo address.
    Optional<User> findByEmailAddress(String address);

    boolean existsByEmailAddress(String address);

    Optional<User> findByResetPasswordToken(String resetPasswordToken);

    Page<User> findByRoleAndActiveTrue(
        UserRole role,
        Pageable pageable
    );

    long countByRoleAndActiveTrue(UserRole role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.role = :role ORDER BY u.id")
    List<User> buscarPorPerfilComBloqueio(
        @Param("role") UserRole role
    );
}