package com.ebiz.wsb.domain.parent.application;

import com.ebiz.wsb.domain.parent.dto.ParentDTO;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.parent.exception.ParentNotFoundException;
import com.ebiz.wsb.domain.parent.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentService {

    private final ParentRepository parentRepository;

    public List<ParentDTO> getAllParents() {
        List<ParentDTO> parentListDTO = new ArrayList<>();
        List<Parent> parentList = parentRepository.findAll();
        for (Parent parent : parentList) {
            ParentDTO parentDTO = ParentDTO.builder()
                            .id(parent.getId())
                            .name(parent.getName())
                            .email(parent.getEmail())
                            .phone(parent.getPhone())
                            .address(parent.getAddress())
                            .imagePath(parent.getImagePath())
                            .password(parent.getPassword())
                            .build();
            parentListDTO.add(parentDTO);
        }
        return parentListDTO;
    }

    public ParentDTO getParentById(Long parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ParentNotFoundException("부모 정보를 찾을 수 없습니다."));
        ParentDTO parentDTO = ParentDTO.builder()
                .id(parent.getId())
                .name(parent.getName())
                .email(parent.getEmail())
                .phone(parent.getPhone())
                .address(parent.getAddress())
                .imagePath(parent.getImagePath())
                .password(parent.getPassword())
                .build();
        return parentDTO;
    }

    public ParentDTO updateParent(Long parentId, ParentDTO parentDTO) {
        // 1. 기존 Parent 엔티티를 조회
        Parent existingParent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ParentNotFoundException("부모 정보를 찾을 수 없습니다."));

        // 2. 기존 비밀번호를 유지하고, 나머지 필드만 업데이트
        Parent parent = Parent.builder()
                .id(existingParent.getId())
                .email(parentDTO.getEmail())
                .name(parentDTO.getName())
                .phone(parentDTO.getPhone())
                .address(parentDTO.getAddress())
                .imagePath(parentDTO.getImagePath())
                .password(existingParent.getPassword())  // 기존 비밀번호 유지
                .build();

        // 3. 업데이트된 엔티티를 저장
        Parent updatedParent = parentRepository.save(parent);

        // 4. 저장된 엔티티를 DTO로 변환하여 반환
        return ParentDTO.builder()
                .id(updatedParent.getId())
                .email(updatedParent.getEmail())
                .name(updatedParent.getName())
                .phone(updatedParent.getPhone())
                .address(updatedParent.getAddress())
                .imagePath(updatedParent.getImagePath())
                .build();
    }

    public void deleteParent(Long parentsId) {
        parentRepository.deleteById(parentsId);
    }
}
