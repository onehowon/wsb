package com.ebiz.wsb.domain.schedule.api;

import com.ebiz.wsb.domain.schedule.application.ScheduleService;
import com.ebiz.wsb.domain.schedule.dto.ScheduleByMonthResponseDTO;
import com.ebiz.wsb.domain.schedule.dto.ScheduleDTO;

import java.time.LocalDate;

import com.ebiz.wsb.domain.schedule.dto.ScheduleResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/group/date")
    public ResponseEntity<ScheduleResponseDTO> getGroupScheduleByDate(
            @RequestParam("specificDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate specificDate) {
        ScheduleResponseDTO groupSchedules = scheduleService.getGroupScheduleByDate(specificDate);
        return ResponseEntity.ok(groupSchedules);
    }

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
