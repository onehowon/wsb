package com.ebiz.wsb.domain.schedule.repository;

import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.schedule.entity.Schedule;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {


    List<Schedule> findByGroupIdAndTimeBetween(Long groupId, LocalTime startTime, LocalTime endTime);


    List<Schedule> findByGuardianAndTimeBetween(Guardian guardian, LocalDateTime startTime, LocalDateTime endTime);
}
