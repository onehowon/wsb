package com.ebiz.wsb.domain.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor

public class ScheduleByMonthResponseDTO {
    private final String month;
    private final List<DayScheduleDTO> schedules;
}
