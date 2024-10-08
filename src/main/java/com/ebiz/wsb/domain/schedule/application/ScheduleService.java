package com.ebiz.wsb.domain.schedule.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
import com.ebiz.wsb.domain.schedule.dto.ScheduleDTO;
import com.ebiz.wsb.domain.schedule.entity.Schedule;
import com.ebiz.wsb.domain.schedule.exception.ScheduleAccessException;
import com.ebiz.wsb.domain.schedule.exception.ScheduleNotFoundException;
import com.ebiz.wsb.domain.schedule.repository.ScheduleRepository;
import com.ebiz.wsb.global.service.S3Service;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Guard;
import java.time.LocalDateTime;

@Service
@Builder
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final GuardianRepository guardianRepository;
    private final S3Service s3Service;
    private final UserDetailsServiceImpl userDetailsService;
    private final GroupRepository groupRepository;

    private static final String FILE_UPLOAD_DIRECTORY = "/uploads";

    @Transactional
    public ScheduleDTO createSchedule(ScheduleDTO scheduleDTO, MultipartFile scheduleFile) {


        Guardian guardian = findCurrentGuardian();
        if (guardian == null) {
            throw new IllegalArgumentException("현재 로그인된 사용자로부터 Guardian 정보를 찾을 수 없습니다.");
        }

        String scheduleFileUrl = null;
        if (scheduleFile != null && !scheduleFile.isEmpty()) {
            try {

                scheduleFileUrl = s3Service.uploadScheduleFile(scheduleFile, "walkingschoolbus-bucket");
            } catch (IOException e) {
                throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
            }
        }


        if (scheduleDTO.getRegistrationDate() == null) {
            scheduleDTO.setRegistrationDate(LocalDateTime.now());
        }


        Schedule schedule = Schedule.builder()
                .guardian(guardian)
                .registrationDate(scheduleDTO.getRegistrationDate())
                .scheduleFile(scheduleFileUrl)
                .build();

        schedule = scheduleRepository.save(schedule);

        return convertToDTO(schedule);
    }

    @Transactional(readOnly = true)
    public List<ScheduleDTO> getScheduleForCurrentUser() {
        Guardian currentGuardian = (Guardian) userDetailsService.getUserByContextHolder();

        List<Schedule> schedules = scheduleRepository.findByGuardian(currentGuardian);

        return schedules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduleDTO> getScheduleByDateRange(LocalDateTime startDate, LocalDateTime endDate){
        Guardian currentGuardian = (Guardian) userDetailsService.getUserByContextHolder();

        List<Schedule> schedules = scheduleRepository.findByGuardianAndRegistrationDateBetween(
                currentGuardian, startDate, endDate
        );

        return schedules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduleDTO> getGroupScheduleByDate(Long groupId, LocalDateTime specificDate) {
        Guardian currentGuardian = (Guardian) userDetailsService.getUserByContextHolder();

        boolean isMember = groupRepository.isUserInGroupForGuardian(currentGuardian.getId(), groupId);
        if (!isMember) {
            throw new ScheduleAccessException("해당 그룹의 스케줄에 접근할 권한이 없습니다.");
        }

        LocalDateTime startOfDay = specificDate.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = specificDate.toLocalDate().atTime(23, 59, 59);

        List<Schedule> schedules = scheduleRepository.findByGroupIdAndRegistrationDateBetween(
                groupId, startOfDay, endOfDay
        );

        return schedules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduleDTO> getScheduleByMonth(int year, int month){
        Guardian currentGuardian = (Guardian) userDetailsService.getUserByContextHolder();

        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);

        List<Schedule> schedules = scheduleRepository.findByGuardianAndRegistrationDateBetween(
                currentGuardian, startOfMonth, endOfMonth
        );

        return schedules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleDTO updateSchedule(Long scheduleId, ScheduleDTO scheduleDTO, MultipartFile scheduleFile) {
        Schedule existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("스케줄을 찾을 수 없습니다."));

        String scheduleFileUrl = existingSchedule.getScheduleFile();
        if (scheduleFile != null && !scheduleFile.isEmpty()) {
            try {
                scheduleFileUrl = s3Service.uploadScheduleFile(scheduleFile, "walkingschoolbus-bucket");
            } catch (IOException e) {
                throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
            }
        }


        LocalDateTime registrationDate = scheduleDTO.getRegistrationDate() != null
                ? scheduleDTO.getRegistrationDate()
                : existingSchedule.getRegistrationDate() != null ? existingSchedule.getRegistrationDate() : LocalDateTime.now();

        Guardian guardian = guardianRepository.findById(scheduleDTO.getGuardianId())
                .orElseThrow(() -> new IllegalArgumentException("해당 인솔자를 찾을 수 없습니다."));

        existingSchedule = Schedule.builder()
                .scheduleId(existingSchedule.getScheduleId())
                .guardian(guardian)
                .registrationDate(registrationDate)
                .scheduleFile(scheduleFileUrl != null ? scheduleFileUrl : existingSchedule.getScheduleFile())
                .build();

        scheduleRepository.save(existingSchedule);

        return convertToDTO(existingSchedule);
    }


    @Transactional
    public void deleteSchedule(Long scheduleId){
        if(!scheduleRepository.existsById(scheduleId)){
            throw new ScheduleNotFoundException("스케줄을 찾을 수 없습니다.");
        }
        scheduleRepository.deleteById(scheduleId);
    }

    private void validateScheduleDTO(ScheduleDTO scheduleDTO) {
        if (scheduleDTO.getRegistrationDate() == null) {
            throw new IllegalArgumentException("등록일자는 필수입니다.");
        }
    }

    private ScheduleDTO convertToDTO(Schedule schedule) {
        return ScheduleDTO.builder()
                .scheduleId(schedule.getScheduleId())
                .guardianId(schedule.getGuardian().getId())
                .registrationDate(schedule.getRegistrationDate())
                .scheduleFile(schedule.getScheduleFile()) // scheduleFile 추가
                .build();
    }

    private Guardian findCurrentGuardian() {

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();


        return guardianRepository.findGuardianByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("현재 사용자에 대한 Guardian을 찾을 수 없습니다."));
    }
}
