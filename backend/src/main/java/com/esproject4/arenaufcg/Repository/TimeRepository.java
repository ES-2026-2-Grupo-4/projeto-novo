package com.esproject4.arenaufcg.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.esproject4.arenaufcg.Model.Time;
import java.util.UUID;

public interface TimeRepository extends JpaRepository<Time, Long> {
    
    List<Time> findByDirigenteId(UUID dirigenteId);

}
