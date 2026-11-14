package com.esproject4.arenaufcg.Repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.esproject4.arenaufcg.Model.User;

public interface UserRepository extends JpaRepository<User, UUID> {

}
