package com.condotrack.backend.repository;

import com.condotrack.backend.model.Communication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommunicationRepository extends JpaRepository<Communication, UUID> {
    Page<Communication> findAllByOrderBySentAtDesc(Pageable pageable);
}
