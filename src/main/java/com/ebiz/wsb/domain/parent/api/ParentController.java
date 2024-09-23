package com.ebiz.wsb.domain.parent.api;

import com.ebiz.wsb.domain.parent.application.ParentService;
import com.ebiz.wsb.domain.parent.dto.ParentDTO;
import com.ebiz.wsb.global.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/parents")
@Slf4j
public class ParentController {

    private final ParentService parentService;

    @GetMapping
    public ResponseEntity<List<ParentDTO>> getAllParents() {
        List<ParentDTO> parents = parentService.getAllParents();
        return ResponseEntity.ok(parents);
    }

    @GetMapping("/{parentsId}")
    public ResponseEntity<ParentDTO> getParent(@PathVariable Long parentsId) {
        ParentDTO parentDTO = parentService.getParentById(parentsId);
        return ResponseEntity.ok(parentDTO);
    }

    @PutMapping("/address/{parentsId}")
    public ResponseEntity<ParentDTO> updateParentAddress(
            @PathVariable Long parentsId,
            @RequestParam("address") String address) {

        ParentDTO parentDTO = ParentDTO.builder()
                .id(parentsId)
                .address(address)
                .build();

        ParentDTO updatedParentDTO = parentService.updateParentAddress(parentsId, parentDTO);
        return ResponseEntity.ok(updatedParentDTO);
    }

    @PutMapping("/image/{parentsId}")
    public ResponseEntity<ParentDTO> updateParentImage(
            @PathVariable Long parentsId,
            @RequestParam(value = "imagePath",required = false) MultipartFile imagePath) {

        ParentDTO updatedParentDTO = parentService.updateParentImage(parentsId, imagePath);
        return ResponseEntity.ok(updatedParentDTO);
    }

    @DeleteMapping("/{parentsId}")
    public ResponseEntity<BaseResponse> deleteParent(@PathVariable Long parentsId) {
        parentService.deleteParent(parentsId);
        return ResponseEntity.ok(BaseResponse.builder().message("부모 정보가 삭제되었습니다.").build());
    }
}
