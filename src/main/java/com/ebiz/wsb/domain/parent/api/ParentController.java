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

    @PutMapping("/{parentsId}")
    public ResponseEntity<ParentDTO> updateParent(
            @PathVariable Long parentsId,
            @RequestBody ParentDTO parentDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {

        ParentDTO updatedParentDTO = parentService.updateParent(parentsId, parentDTO, imageFile);
        return ResponseEntity.ok(updatedParentDTO);
    }

    @DeleteMapping("/{parentsId}")
    public ResponseEntity<BaseResponse> deleteParent(@PathVariable Long parentsId) {
        parentService.deleteParent(parentsId);
        return ResponseEntity.ok(BaseResponse.builder().message("부모 정보가 삭제되었습니다.").build());
    }
}
