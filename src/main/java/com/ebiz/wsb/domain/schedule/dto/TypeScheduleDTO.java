package com.ebiz.wsb.domain.schedule.dto;

import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
import com.ebiz.wsb.domain.guardian.dto.GuardianSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TypeScheduleDTO {
    private Long id;
    private String type;
    private String time;
    private List<GuardianSummaryDTO> guardianList;
}
