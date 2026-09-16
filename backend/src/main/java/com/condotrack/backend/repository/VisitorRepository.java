package com.condotrack.backend.repository;

import com.condotrack.backend.model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VisitorRepository extends JpaRepository<Visitor, UUID> {
}
