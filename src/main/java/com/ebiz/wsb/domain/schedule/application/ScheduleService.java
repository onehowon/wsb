package com.ebiz.wsb.domain.schedule.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.exception.GroupNotFoundException;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.guardian.dto.GuardianSummaryDTO;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
import com.ebiz.wsb.domain.notification.application.PushNotificationService;
import com.ebiz.wsb.domain.notification.dto.PushType;
import com.ebiz.wsb.domain.schedule.dto.*;
import com.ebiz.wsb.domain.schedule.entity.Schedule;
import com.ebiz.wsb.domain.schedule.entity.ScheduleType;
import com.ebiz.wsb.domain.schedule.exception.ScheduleAccessException;
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
    private final PushNotificationService pushNotificationService;

    @Transactional
    public ScheduleDTO createSchedule(ScheduleDTO scheduleDTO) {
        Guardian guardian = findCurrentGuardian();

        Group group = guardian.getGroup();

        if (group == null) {
            throw new GroupNotFoundException("지도사에게 그룹이 할당되어 있지 않습니다.");
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

        Map<String, String> pushData = pushNotificationService.createPushData(PushType.SCHEDULE);
        sendPushNotificationToGroup(group.getId(), pushData.get("title"), pushData.get("body"), PushType.SCHEDULE);

        return convertScheduleToDTO(schedule);
    }

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
                        .scheduleType(schedule.getScheduleType() != null ? schedule.getScheduleType().getName() : "N/A")
                        .scheduleId(schedule.getScheduleId())
                        .groupId(schedule.getGroup() != null ? schedule.getGroup().getId() : null)
                        .build())
                .collect(Collectors.toList());

        return ScheduleByMonthResponseDTO.builder()
                .month(year + "-" + (month < 10 ? "0" + month : month))
                .schedules(daySchedules)
                .build();
    }

    @Transactional
    public ScheduleDTO updateSchedule(Long scheduleId, ScheduleDTO scheduleDTO) {
        Guardian currentGuardian = findCurrentGuardian();

        Schedule existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("스케줄을 찾을 수 없습니다."));

        if (!existingSchedule.getGroup().getId().equals(currentGuardian.getGroup().getId())) {
            throw new ScheduleAccessException("해당 스케줄을 수정할 권한이 없습니다.");
        }

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
        Guardian currentGuardian = findCurrentGuardian();

        Schedule existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("스케줄을 찾을 수 없습니다."));

        if (!existingSchedule.getGroup().getId().equals(currentGuardian.getGroup().getId())) {
            throw new ScheduleAccessException("해당 스케줄을 삭제할 권한이 없습니다.");
        }

        scheduleRepository.deleteById(scheduleId);
    }

    private ScheduleResponseDTO convertToResponseDTO(List<Schedule> schedules, LocalDate date) {
        if (schedules.isEmpty()) {
            System.out.println("해당 일에 지정된 스케줄이 없습니다.: " + date);
            return ScheduleResponseDTO.builder()
                    .scheduleBasicInfo(ScheduleBasicInfoDTO.builder()
                            .groupId(null)
                            .scheduleId(null)
                            .day(date)
                            .build())
                    .typeSchedules(List.of())
                    .build();
        }

        Schedule firstSchedule = schedules.get(0);
        System.out.println("첫 번째 스케줄: " + firstSchedule);

        Long groupId = firstSchedule.getGroup() != null ? firstSchedule.getGroup().getId() : null;
        Long scheduleId = firstSchedule.getScheduleId();

        List<TypeScheduleDTO> typeSchedules = schedules.stream()
                .map(schedule -> TypeScheduleDTO.builder()
                        .type(schedule.getScheduleType() != null ? schedule.getScheduleType().getName() : "N/A")
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
                        .groupId(groupId)
                        .scheduleId(scheduleId)
                        .day(date)
                        .build())
                .typeSchedules(typeSchedules)
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
                .id(schedule.getScheduleType() != null ? schedule.getScheduleType().getId() : null)
                .type(schedule.getScheduleType() != null ? schedule.getScheduleType().getName() : "N/A")
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
        Guardian guardian = guardianRepository.findGuardianByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("현재 사용자에 대한 지도사를 찾을 수 없습니다."));

        if (guardian.getGroup() == null) {
            throw new ScheduleAccessException("해당 지도사는 그룹에 속해 있지 않습니다.");
        }

        return guardian;
    }

    private void sendPushNotificationToGroup(Long groupId, String title, String body, PushType pushType) {
        pushNotificationService.sendPushNotificationToGroup(groupId, title, body, pushType);
    }
}
