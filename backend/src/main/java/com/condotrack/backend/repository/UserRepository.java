//---------------- milestone 17.1 ---------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Page<User> findAllByOrderByLastNameAscFirstNameAsc(Pageable pageable);
    @Query("select r.code from User u join u.roles r where lower(u.email) = lower(:email)")
    Set<String> findRoleCodesByEmailIgnoreCase(@Param("email") String email);
}