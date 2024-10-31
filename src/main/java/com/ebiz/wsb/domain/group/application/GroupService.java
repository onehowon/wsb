package com.ebiz.wsb.domain.group.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.dto.GroupDTO;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.exception.GroupAlreadyActiveException;
import com.ebiz.wsb.domain.group.exception.GroupNotAccessException;
import com.ebiz.wsb.domain.group.exception.GroupNotFoundException;
import com.ebiz.wsb.domain.group.exception.GuideNotOnDutyException;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserDetailsServiceImpl userDetailsService;

    @Transactional
    public GroupDTO startGuide() {
        // 현재 사용자 정보를 가져와 인솔자인지 확인
        Object userByContextHolder = userDetailsService.getUserByContextHolder();
        if (!(userByContextHolder instanceof Guardian)) {
            throw new GuardianNotFoundException("해당 지도사를 찾을 수 없습니다");
        }

        Guardian guardian = (Guardian) userByContextHolder;

        // 인솔자가 속한 그룹 정보를 조회
        Group group = groupRepository.findById(guardian.getGroup().getId())
                .orElseThrow(() -> new GroupNotFoundException("해당 그룹을 찾을 수 없습니다"));

        // 다른 인솔자가 이미 출근을 시작한 경우 예외 발생
        if (group.getIsGuideActive()) {
            throw new GroupAlreadyActiveException("해당 그룹은 이미 운행 중입니다");
        }

        // 출근 정보 업데이트
        Group updateGroup = group.toBuilder()
                .isGuideActive(true)
                .dutyGuardianId(guardian.getId())
                .build();

        groupRepository.save(updateGroup);

        // 업데이트된 그룹 정보를 DTO로 변환하여 반환
        return GroupDTO.builder()
                .id(updateGroup.getId())
                .groupName(updateGroup.getGroupName())
                .schoolName(updateGroup.getSchoolName())
                .isGuideActive(updateGroup.getIsGuideActive())
                .dutyGuardianId(updateGroup.getDutyGuardianId())
                .build();
    }

    @Transactional
    public GroupDTO stopGuide() {
        // 현재 사용자 정보를 가져와 인솔자인지 확인
        Object userByContextHolder = userDetailsService.getUserByContextHolder();
        if (!(userByContextHolder instanceof Guardian)) {
            throw new GuardianNotFoundException("해당 지도사를 찾을 수 없습니다");
        }

        Guardian guardian = (Guardian) userByContextHolder;

        // 인솔자가 속한 그룹 정보 가져오기
        Group group = groupRepository.findById(guardian.getGroup().getId())
                .orElseThrow(() -> new GroupNotFoundException("해당 그룹을 찾을 수 없습니다"));

        // 현재 출근 상태인지, 그리고 출근한 인솔자가 요청한 인솔자와 일치하는지 확인
        if (!group.getIsGuideActive() || !guardian.getId().equals(group.getDutyGuardianId())) {
            throw new GuideNotOnDutyException("해당 지도사는 퇴근하기의 권한이 없습니다");
        }

        // 출근 상태를 해제하고 dutyGuardianId를 null로 설정
        Group updateGroup = group.toBuilder()
                .isGuideActive(false)
                .dutyGuardianId(null)
                .build();

        groupRepository.save(updateGroup);

        // 업데이트된 그룹 정보를 DTO로 반환
        return GroupDTO.builder()
                .id(updateGroup.getId())
                .groupName(updateGroup.getGroupName())
                .schoolName(updateGroup.getSchoolName())
                .isGuideActive(updateGroup.getIsGuideActive())
                .dutyGuardianId(updateGroup.getDutyGuardianId())
                .build();
    }

}
