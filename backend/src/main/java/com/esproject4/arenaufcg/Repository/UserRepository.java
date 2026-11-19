package com.esproject4.arenaufcg.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esproject4.arenaufcg.Model.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    // email.address: o Spring Data navega pelo @Embedded Email até o campo address.
    Optional<User> findByEmailAddress(String address);

    boolean existsByEmailAddress(String address);

    Optional<User> findByResetPasswordToken(String resetPasswordToken);
}