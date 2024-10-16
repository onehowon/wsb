package com.ebiz.wsb.domain.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DayScheduleDTO {
    private final LocalDate day;
    private final String time;
    private final String scheduleType;
    private final Long scheduleId;
    private final Long groupId;
}
