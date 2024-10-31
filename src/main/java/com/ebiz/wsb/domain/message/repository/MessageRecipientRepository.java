package com.ebiz.wsb.domain.message.repository;

import com.ebiz.wsb.domain.message.entity.MessageRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRecipientRepository extends JpaRepository<MessageRecipient, Long> {

    List<MessageRecipient> findByGuardianId(Long guardianId);
    Optional<MessageRecipient> findByMessage_messageIdAndGuardian_Id(Long messageId, Long guardianId);
}
