package com.ebiz.wsb.domain.schedule.api;

import com.amazonaws.Response;
import com.ebiz.wsb.domain.schedule.application.ScheduleService;
import com.ebiz.wsb.domain.schedule.dto.ScheduleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ScheduleDTO> createSchedule(
            @ModelAttribute ScheduleDTO scheduleDTO,
            @RequestParam("file") MultipartFile file){
        ScheduleDTO createdSchedule = scheduleService.createSchedule(scheduleDTO,file);
        return ResponseEntity.ok(createdSchedule);
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDTO> getScheduleById(@PathVariable Long scheduleId){
        ScheduleDTO scheduleDTO = scheduleService.getScheduleById(scheduleId);
        return ResponseEntity.ok(scheduleDTO);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDTO> updateSchedule(
            @PathVariable Long scheduleId,
            @ModelAttribute ScheduleDTO scheduleDTO,
            @RequestParam("file") MultipartFile file){
        ScheduleDTO updatedSchedule = scheduleService.updateSchedule(scheduleId, scheduleDTO, file);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId){
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
}
