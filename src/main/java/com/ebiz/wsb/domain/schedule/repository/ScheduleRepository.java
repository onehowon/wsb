package com.ebiz.wsb.domain.schedule.repository;

import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.schedule.entity.Schedule;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByGuardian(Guardian guardian);

    List<Schedule> findByGuardianAndRegistrationDateBetween(Guardian guardian, LocalDateTime startDate, LocalDateTime endDate);
}
