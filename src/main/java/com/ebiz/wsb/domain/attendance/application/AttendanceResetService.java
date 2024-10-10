package com.ebiz.wsb.domain.attendance.application;
import com.ebiz.wsb.domain.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceResetService {

    private final AttendanceRepository attendanceRepository;

    @Value("${schedule.use}")
    private boolean useSchedule;

    // 매일 저녁 7시에 출석 상태 초기화
    @Scheduled(cron = "${schedule.cron}")
    public void resetAttendanceStatus() {
        // 모든 학생의 출석 상태를 초기화
        if(useSchedule) {
            log.info("Attendance records have been reset.");
            attendanceRepository.deleteAll();
        }
    }
}
