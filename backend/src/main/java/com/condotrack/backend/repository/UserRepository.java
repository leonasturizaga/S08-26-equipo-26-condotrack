//--------------- milestone 12 ---------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.User;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.Optional;
// import java.util.UUID;

// public interface UserRepository extends JpaRepository<User, UUID> {
//     Optional<User> findByEmailIgnoreCase(String email);
//     boolean existsByEmailIgnoreCase(String email);
// }


//---------------- milestone 13 ---------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Page<User> findAllByOrderByLastNameAscFirstNameAsc(Pageable pageable);
}