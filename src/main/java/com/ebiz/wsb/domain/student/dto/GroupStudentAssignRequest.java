package com.ebiz.wsb.domain.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class GroupStudentAssignRequest {
    private Long studentId;
    private Long groupId;
}
