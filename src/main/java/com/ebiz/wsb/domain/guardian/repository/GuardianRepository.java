package com.ebiz.wsb.domain.guardian.repository;

import com.ebiz.wsb.domain.guardian.entity.Guardian;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardianRepository extends JpaRepository<Guardian, Long> {
    Optional<Guardian> findGuardianByEmail(String email);
}
