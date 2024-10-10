package com.ebiz.wsb.domain.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleBasicInfoDTO {
    private Long scheduleId;
    private Long groupId;
    private LocalDate day;
}
