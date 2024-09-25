package com.ebiz.wsb.domain.parent.dto;

import com.ebiz.wsb.domain.group.dto.GroupDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParentDTO {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String address;
    private GroupDTO group;
    private String imagePath;
}
