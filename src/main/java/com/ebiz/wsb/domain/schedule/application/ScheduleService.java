package com.ebiz.wsb.domain.schedule.application;

import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
import com.ebiz.wsb.domain.schedule.dto.ScheduleDTO;
import com.ebiz.wsb.domain.schedule.entity.Schedule;
import com.ebiz.wsb.domain.schedule.exception.ScheduleNotFoundException;
import com.ebiz.wsb.domain.schedule.repository.ScheduleRepository;
import com.ebiz.wsb.global.service.S3Service;
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

    private static final String FILE_UPLOAD_DIRECTORY = "/uploads";

    @Transactional
    public ScheduleDTO createSchedule(ScheduleDTO scheduleDTO, MultipartFile scheduleFile) {

        // Guardian ID를 전달받지 않으므로, 현재 로그인된 사용자로부터 Guardian 정보를 가져옴
        Guardian guardian = findCurrentGuardian();
        if (guardian == null) {
            throw new IllegalArgumentException("현재 로그인된 사용자로부터 Guardian 정보를 찾을 수 없습니다.");
        }

        String scheduleFileUrl = null;
        if (scheduleFile != null && !scheduleFile.isEmpty()) {
            try {
                // S3에 PDF 또는 XLSX 파일을 업로드
                scheduleFileUrl = s3Service.uploadScheduleFile(scheduleFile, "walkingschoolbus-bucket");
            } catch (IOException e) {
                throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
            }
        }

        // 등록일자가 null인 경우, 현재 시간으로 설정
        if (scheduleDTO.getRegistrationDate() == null) {
            scheduleDTO.setRegistrationDate(LocalDateTime.now());
        }

        // Schedule 엔티티 생성
        Schedule schedule = Schedule.builder()
                .guardian(guardian)
                .registrationDate(scheduleDTO.getRegistrationDate())
                .scheduleFile(scheduleFileUrl)
                .build();

        schedule = scheduleRepository.save(schedule);

        return convertToDTO(schedule);
    }

    public ScheduleDTO getScheduleById(Long scheduleId){
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("스케줄울 찾을 수 없습니다."));

        return convertToDTO(schedule);
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

        // 등록일자가 null인 경우, 기존 등록일자를 유지하거나 현재 시간으로 설정
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
        // Spring Security에서 현재 사용자 이메일 가져오기
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 이메일로 Guardian을 찾는 로직
        return guardianRepository.findGuardianByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("현재 사용자에 대한 Guardian을 찾을 수 없습니다."));
    }
}
