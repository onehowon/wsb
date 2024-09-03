package com.ebiz.wsb.domain.auth.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpRequest {
    private String email;
    private String password;
    private UserType userType; // Enum으로 사용자 유형 추가
    private String name;
}

