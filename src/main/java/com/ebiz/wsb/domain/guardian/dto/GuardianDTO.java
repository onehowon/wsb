package com.ebiz.wsb.domain.guardian.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class GuardianDTO {
    private Long id;
    private String name;
    private String email;
    private String bio;
    private String experience;
    private String phone;
    private String imagePath;
    private Long groupId;
}
