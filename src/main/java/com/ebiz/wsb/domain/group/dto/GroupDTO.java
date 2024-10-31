package com.ebiz.wsb.domain.group.dto;

import com.ebiz.wsb.domain.attendance.entity.AttendanceMessageType;
import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
import com.ebiz.wsb.domain.parent.dto.ParentDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {

    private Long id;
    private String groupName;
    private String schoolName;
    private Boolean isGuideActive;
    private Long dutyGuardianId;
    private AttendanceMessageType messageType;
}
