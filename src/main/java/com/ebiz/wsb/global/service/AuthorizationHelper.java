package com.ebiz.wsb.global.service;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotAccessException;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.parent.exception.ParentAccessException;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.student.exception.StudentNotAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorizationHelper {

    private final UserDetailsServiceImpl userDetailsService;

    public Object getCurrentUser(String userType) {
        return userDetailsService.getUserTypeByContextHolder(userType);
    }

    public Parent getLoggedInParent() {
        Object currentUser = getCurrentUser("PARENT");
        if (currentUser instanceof Parent) {
            return (Parent) currentUser;
        }
        throw new ParentAccessException("학부모만 이 작업을 수행할 수 있습니다.");
    }

    public Guardian getLoggedInGuardian() {
        Object currentUser = getCurrentUser("GUARDIAN");
        if (currentUser instanceof Guardian) {
            return (Guardian) currentUser;
        }
        throw new GuardianNotAccessException("지도사만 이 작업을 수행할 수 있습니다.");
    }

    public void validateParentAccess(Parent parent, Long parentId) {
        if (!parent.getId().equals(parentId)) {
            throw new ParentAccessException("권한이 없습니다.");
        }
    }

    public void validateGuardianAccess(Guardian guardian, Long guardianId) {
        if (!guardian.getId().equals(guardianId)) {
            throw new GuardianNotAccessException("권한이 없습니다.");
        }
    }
}
