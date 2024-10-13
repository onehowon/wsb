package com.ebiz.wsb.domain.schedule.repository;

import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.schedule.entity.Schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("SELECT s FROM Schedule s WHERE s.group.id = :groupId AND s.day = :day")
    List<Schedule> findByGroupIdAndDay(@Param("groupId") Long groupId, @Param("day") LocalDate day);



    List<Schedule> findByGuardiansAndDayBetween(Guardian guardian, LocalDate startDay, LocalDate endDay);
}
