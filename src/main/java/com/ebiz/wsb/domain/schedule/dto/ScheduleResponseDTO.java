package com.ebiz.wsb.domain.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleResponseDTO {
    private ScheduleBasicInfoDTO scheduleBasicInfo;
    private List<TypeScheduleDTO> typeSchedules;
}
