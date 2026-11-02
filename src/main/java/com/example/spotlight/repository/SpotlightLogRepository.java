package com.example.spotlight.repository;

import com.example.spotlight.domain.SpotlightLog;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpotlightLogRepository extends JpaRepository<SpotlightLog, Long> {

    Optional<SpotlightLog> findByResourceCode(String resourceCode);

    @Query("SELECT s FROM SpotlightLog s WHERE s.ownerId = :ownerId ORDER BY s.appliedDate DESC")
    List<SpotlightLog> findByOwnerId(@Param("ownerId") Long ownerId);
}
