package com.ebiz.wsb.domain.parent.dto;

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
    private String phone;
}
