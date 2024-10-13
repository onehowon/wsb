package com.ebiz.wsb.domain.schedule.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.exception.GroupNotFoundException;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.guardian.dto.GuardianSummaryDTO;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
import com.ebiz.wsb.domain.schedule.dto.*;
import com.ebiz.wsb.domain.schedule.entity.Schedule;
import com.ebiz.wsb.domain.schedule.entity.ScheduleType;
import com.ebiz.wsb.domain.schedule.exception.ScheduleNotFoundException;
import com.ebiz.wsb.domain.schedule.repository.ScheduleRepository;


import com.ebiz.wsb.domain.schedule.repository.ScheduleTypeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Builder
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final GuardianRepository guardianRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final GroupRepository groupRepository;
    private final ScheduleTypeRepository scheduleTypeRepository;

    @Transactional
    public ScheduleDTO createSchedule(ScheduleDTO scheduleDTO) {
        Guardian guardian = findCurrentGuardian();

        Group group = guardian.getGroup();

        if (group == null) {
            throw new GroupNotFoundException("인솔자에게 그룹이 할당되어 있지 않습니다.");
        }

        List<Guardian> guardians = guardianRepository.findAllById(
                scheduleDTO.getGuardianList().stream()
                        .map(GuardianSummaryDTO::getId)
                        .collect(Collectors.toList())
        );

        ScheduleType scheduleType = scheduleTypeRepository.findById(scheduleDTO.getScheduleTypes().get(0).getId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 스케줄 유형 ID가 아닙니다."));

        Schedule schedule = Schedule.builder()
                .group(group)
                .guardians(guardians)
                .scheduleType(scheduleType)
                .day(scheduleDTO.getDay())
                .time(scheduleDTO.getTime())
                .build();

        schedule = scheduleRepository.save(schedule);

        return convertScheduleToDTO(schedule);
    }

    // 1. 일별 스케줄 조회
    @Transactional(readOnly = true)
    public ScheduleResponseDTO getGroupScheduleByDate(LocalDate specificDate) {
        Guardian currentGuardian = findCurrentGuardian();
        Group group = currentGuardian.getGroup();

        List<Schedule> schedules = scheduleRepository.findByGroupIdAndDay(group.getId(), specificDate);

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

        List<DayScheduleDTO> daySchedules = schedules.stream()
                .map(schedule -> DayScheduleDTO.builder()
                        .day(schedule.getDay())
                        .time(schedule.getTime().toString())
                        .scheduleType(schedule.getScheduleType().getName())
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

        Schedule updatedSchedule = Schedule.builder()
                .scheduleId(existingSchedule.getScheduleId())
                .group(existingSchedule.getGroup())
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
                        .type(schedule.getScheduleType() != null ? schedule.getScheduleType().getName() : "ScheduleType 존재하지 않음")
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
                .typeSchedules(typeSchedules)  // 복원된 타입 스케줄 리스트 반환
                .build();
    }

    private ScheduleDTO convertScheduleToDTO(Schedule schedule) {
        List<GuardianSummaryDTO> guardianList = schedule.getGuardians().stream()
                .map(guardian -> GuardianSummaryDTO.builder()
                        .id(guardian.getId())
                        .name(guardian.getName())
                        .imagePath(guardian.getImagePath())
                        .build())
                .collect(Collectors.toList());

        TypeScheduleDTO scheduleTypeDTO = TypeScheduleDTO.builder()
                .id(schedule.getScheduleType().getId())
                .type(schedule.getScheduleType().getName())
                .time(schedule.getTime().toString())
                .guardianList(guardianList)
                .build();

        return ScheduleDTO.builder()
                .scheduleId(schedule.getScheduleId())
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
