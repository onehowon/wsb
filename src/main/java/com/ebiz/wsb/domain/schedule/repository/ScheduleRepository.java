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


    // 특정 Guardian의 스케줄을 시간 범위로 조회 (LocalTime 사용)
    List<Schedule> findByGuardianAndTimeBetween(Guardian guardian, LocalDateTime start, LocalDateTime end);

    // 그룹 ID로 특정 시간 범위의 스케줄 조회 (LocalTime 사용)
    List<Schedule> findByGroupIdAndTimeBetween(Long groupId, LocalTime startTime, LocalTime endTime);
}
