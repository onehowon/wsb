package com.ebiz.wsb.domain.schedule.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
import com.ebiz.wsb.domain.guardian.dto.GuardianSummaryDTO;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
import com.ebiz.wsb.domain.schedule.dto.*;
import com.ebiz.wsb.domain.schedule.entity.Schedule;
import com.ebiz.wsb.domain.schedule.entity.ScheduleType;
import com.ebiz.wsb.domain.schedule.exception.ScheduleAccessException;
import com.ebiz.wsb.domain.schedule.exception.ScheduleNotFoundException;
import com.ebiz.wsb.domain.schedule.repository.ScheduleRepository;
import com.ebiz.wsb.domain.schedule.repository.ScheduleTypeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Builder
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final GuardianRepository guardianRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final ScheduleTypeRepository scheduleTypeRepository;
    private final GroupRepository groupRepository;

    @Transactional
    public ScheduleDTO createSchedule(ScheduleDTO scheduleDTO) {
        Group group = groupRepository.findById(scheduleDTO.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 그룹 ID를 제공해야 합니다."));

        ScheduleType scheduleType = scheduleTypeRepository.findById(scheduleDTO.getScheduleTypes().get(0).getId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 스케줄 유형 ID가 아닙니다."));

        List<Guardian> guardians = guardianRepository.findAllById(
                scheduleDTO.getGuardianList().stream()
                        .map(GuardianSummaryDTO::getId)
                        .collect(Collectors.toList())
        );

        Schedule schedule = Schedule.builder()
                .group(group)
                .scheduleType(scheduleType)
                .guardians(guardians)
                .day(scheduleDTO.getDay())
                .time(scheduleDTO.getTime())
                .build();

        schedule = scheduleRepository.save(schedule);

        return convertScheduleToDTO(schedule);
    }

    // 1. 일별 스케줄 조회
    @Transactional(readOnly = true)
    public ScheduleResponseDTO getGroupScheduleByDate(Long groupId, LocalDate specificDate) {
        List<Schedule> schedules = scheduleRepository.findByGroupIdAndDay(groupId, specificDate);

        return convertToResponseDTO(schedules, specificDate);
    }
    @Transactional(readOnly = true)
    public ScheduleByMonthResponseDTO getMyScheduleByMonth(int year, int month) {
        Guardian currentGuardian = findCurrentGuardian();

        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        List<Schedule> schedules = scheduleRepository.findByGuardiansAndDayBetween(
                currentGuardian, startOfMonth, endOfMonth
        );

        Map<LocalDate, List<String>> schedulesByDay = schedules.stream()
                .collect(Collectors.groupingBy(
                        Schedule::getDay,
                        Collectors.mapping(schedule -> schedule.getScheduleType().getName(), Collectors.toList())
                ));

        List<DayScheduleDTO> daySchedules = schedulesByDay.entrySet().stream()
                .map(entry -> DayScheduleDTO.builder()
                        .day(entry.getKey())
                        .scheduleTypes(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return ScheduleByMonthResponseDTO.builder()
                .month(year + "-" + (month < 10 ? "0" + month : month))
                .schedules(daySchedules)
                .build();
    }

    @Transactional
    public ScheduleDTO updateSchedule(Long scheduleId, ScheduleDTO scheduleDTO) {
        Schedule existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("스케줄을 찾을 수 없습니다."));

        ScheduleType scheduleType = scheduleTypeRepository.findById(scheduleDTO.getScheduleTypes().get(0).getId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 스케줄 유형 ID가 아닙니다."));

        Schedule updatedSchedule = Schedule.builder()
                .scheduleId(existingSchedule.getScheduleId())
                .group(existingSchedule.getGroup())
                .scheduleType(scheduleType)
                .day(scheduleDTO.getDay())
                .time(scheduleDTO.getTime())
                .build();

        updatedSchedule = scheduleRepository.save(updatedSchedule);

        return convertScheduleToDTO(updatedSchedule);
    }


    @Transactional
    public void deleteSchedule(Long scheduleId){
        if(!scheduleRepository.existsById(scheduleId)){
            throw new ScheduleNotFoundException("스케줄을 찾을 수 없습니다.");
        }
        scheduleRepository.deleteById(scheduleId);
    }

    private ScheduleResponseDTO convertToResponseDTO(List<Schedule> schedules, LocalDate date) {
        if (schedules.isEmpty()) {
            return ScheduleResponseDTO.builder()
                    .scheduleBasicInfo(ScheduleBasicInfoDTO.builder()
                            .groupId(null)
                            .scheduleId(null)
                            .day(date)
                            .build())
                    .typeSchedules(List.of())
                    .build();
        }


        List<TypeScheduleDTO> typeSchedules = schedules.stream()
                .map(schedule -> TypeScheduleDTO.builder()
                        .type(schedule.getScheduleType().getName())
                        .time(schedule.getTime().toString())
                        .guardianList(schedule.getGuardians().stream()
                                .map(guardian -> GuardianSummaryDTO.builder()
                                        .name(guardian.getName())
                                        .imagePath(guardian.getImagePath())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return ScheduleResponseDTO.builder()
                .scheduleBasicInfo(ScheduleBasicInfoDTO.builder()
                        .groupId(schedules.get(0).getGroup().getId())
                        .scheduleId(schedules.get(0).getScheduleId())
                        .day(date)
                        .build())
                .typeSchedules(typeSchedules)
                .build();
    }

    private ScheduleDTO convertScheduleToDTO(Schedule schedule) {
        List<GuardianSummaryDTO> guardianList = schedule.getGuardians().stream()
                .map(guardian -> GuardianSummaryDTO.builder()
                        .name(guardian.getName())
                        .imagePath(guardian.getImagePath())
                        .build())
                .collect(Collectors.toList());

        ScheduleTypeDTO scheduleTypeDTO = ScheduleTypeDTO.builder()
                .id(schedule.getScheduleType().getId())
                .name(schedule.getScheduleType().getName())
                .build();

        return ScheduleDTO.builder()
                .scheduleId(schedule.getScheduleId())
                .groupId(schedule.getGroup().getId())
                .day(schedule.getDay())
                .time(schedule.getTime())
                .guardianList(guardianList)
                .scheduleTypes(List.of(scheduleTypeDTO))
                .build();
    }



    private Guardian findCurrentGuardian() {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return guardianRepository.findGuardianByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("현재 사용자에 대한 인솔자를 찾을 수 없습니다."));
    }
}
