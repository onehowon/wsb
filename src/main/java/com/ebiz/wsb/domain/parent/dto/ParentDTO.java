package com.ebiz.wsb.domain.parent.dto;

import com.ebiz.wsb.domain.group.dto.GroupDTO;
import com.ebiz.wsb.domain.student.dto.StudentDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParentDTO {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private GroupDTO group;
    private String imagePath;
    private List<StudentDTO> students;
}
