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
    private final List<String> scheduleTypes;
}
