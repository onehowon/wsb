package com.ebiz.wsb.domain.attendance.dto;

import com.ebiz.wsb.domain.attendance.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AttendanceUpdateRequest {

    private Long studentId;
    private AttendanceStatus AttendanceStatus;

}

