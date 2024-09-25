package com.ebiz.wsb.domain.message.repository;

import com.ebiz.wsb.domain.message.entity.MessageRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRecipientRepository extends JpaRepository<MessageRecipient, Long> {

    List<MessageRecipient> findByGuardianId(Long guardianId);
}
