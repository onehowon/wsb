package com.ebiz.wsb.domain.schedule.api;

import com.amazonaws.Response;
import com.ebiz.wsb.domain.schedule.application.ScheduleService;
import com.ebiz.wsb.domain.schedule.dto.ScheduleByMonthResponseDTO;
import com.ebiz.wsb.domain.schedule.dto.ScheduleDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.ebiz.wsb.domain.schedule.dto.ScheduleResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ScheduleDTO> createSchedule(@RequestBody ScheduleDTO scheduleDTO) {
        ScheduleDTO createdSchedule = scheduleService.createSchedule(scheduleDTO);
        return ResponseEntity.ok(createdSchedule);
    }

    // 그룹별 일별 스케줄 조회
    @GetMapping("/group/{groupId}/date")
    public ResponseEntity<ScheduleResponseDTO> getGroupScheduleByDate(
            @PathVariable Long groupId,
            @RequestParam("specificDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate specificDate) {
        ScheduleResponseDTO groupSchedules = scheduleService.getGroupScheduleByDate(groupId, specificDate);
        return ResponseEntity.ok(groupSchedules);
    }

    // 사용자 월별 스케줄 조회
    @GetMapping("/month")
    public ResponseEntity<ScheduleByMonthResponseDTO> getMySchedulesByMonth(
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        ScheduleByMonthResponseDTO myMonthlySchedules = scheduleService.getMyScheduleByMonth(year, month);
        return ResponseEntity.ok(myMonthlySchedules);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDTO> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleDTO scheduleDTO) {
        ScheduleDTO updatedSchedule = scheduleService.updateSchedule(scheduleId, scheduleDTO);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
}
