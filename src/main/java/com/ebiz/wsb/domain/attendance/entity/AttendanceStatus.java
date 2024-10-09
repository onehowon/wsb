package com.ebiz.wsb.domain.attendance.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

public enum AttendanceStatus {
    UNCONFIRMED,  // 미인증
    PRESENT,      // 출석 완료
    ABSENT,       // 결석
    PREABSENT     // 사전 결석
}
