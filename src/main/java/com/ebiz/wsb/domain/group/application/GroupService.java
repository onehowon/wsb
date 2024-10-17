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
    public GroupDTO startGuide(Long groupId) {
        // 해당 그룹 찾기
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("해당 그룹을 찾을 수 없습니다"));

        // 이미 다른 인솔자가 출근을 시작한 경우 처리
        if(group.getIsGuideActive()) {
            throw new GroupAlreadyActiveException("해당 그룹은 이미 운행 중입니다");
        }

        // "출근하기" 버튼 누른 인솔자 찾기
        Object userByContextHolder = userDetailsService.getUserByContextHolder();

        if(userByContextHolder instanceof Guardian) {
            Guardian guardian = (Guardian) userByContextHolder;

            if (guardian.getGroup() == null || !guardian.getGroup().getId().equals(groupId)) {
                throw new GroupNotAccessException("해당 그룹의 출근 권한이 없습니다.");
            }

            if (group.getIsGuideActive()) {
                throw new GroupAlreadyActiveException("해당 그룹은 이미 운행 중입니다");
            }

            // 그룹에 운행 중임을 나타내는 값과 운행 위치를 제공하는 인솔자 ID 기입
            Group updateGroup = group.toBuilder()
                    .isGuideActive(true)
                    .dutyGuardianId(guardian.getId())
                    .build();

            groupRepository.save(updateGroup);

            GroupDTO groupDTO = GroupDTO.builder()
                    .id(updateGroup.getId())
                    .groupName(updateGroup.getGroupName())
                    .schoolName(updateGroup.getSchoolName())
                    .isGuideActive(updateGroup.getIsGuideActive())
                    .dutyGuardianId(updateGroup.getDutyGuardianId())
                    .build();

            return groupDTO;
        } else {
            throw new GuardianNotFoundException("해당 인솔자를 찾을 수 없습니다");
        }
    }

    @Transactional
    public GroupDTO stopGuide(Long groupId) {
        // 해당 그룹 찾기
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("해당 그룹을 찾을 수 없습니다"));

        Object userByContextHolder = userDetailsService.getUserByContextHolder();
        if(userByContextHolder instanceof Guardian) {
            Guardian guardian = (Guardian) userByContextHolder;

            if (guardian.getGroup() == null || !guardian.getGroup().getId().equals(groupId)) {
                throw new GroupNotAccessException("해당 그룹의 퇴근 권한이 없습니다.");
            }

            if(guardian.getId().equals(group.getDutyGuardianId())) {
                Group updateGroup = group.toBuilder()
                        .isGuideActive(false)
                        .dutyGuardianId(null)
                        .build();

                groupRepository.save(updateGroup);

                GroupDTO groupDTO = GroupDTO.builder()
                        .id(updateGroup.getId())
                        .groupName(updateGroup.getGroupName())
                        .schoolName(updateGroup.getSchoolName())
                        .isGuideActive(updateGroup.getIsGuideActive())
                        .dutyGuardianId(updateGroup.getDutyGuardianId())
                        .build();

                return groupDTO;
            }else {
                throw new GuideNotOnDutyException("해당 인솔자는 퇴근하기의 권한이 없습니다");
            }
        } else {
            throw new GuardianNotFoundException("해당 인솔자를 찾을 수 없습니다");
        }
    }
}
