package com.condotrack.backend.media.repository;

import com.condotrack.backend.media.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {
}
