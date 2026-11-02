package com.taplay.backend.repository;

import com.taplay.backend.entity.ArcadeMachineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArcadeMachineRepository extends JpaRepository<ArcadeMachineEntity, String> {
    Optional<ArcadeMachineEntity> findByTagId(String tagId);
}
