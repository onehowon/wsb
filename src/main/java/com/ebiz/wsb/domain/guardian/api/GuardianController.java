package com.ebiz.wsb.domain.guardian.api;

import com.ebiz.wsb.domain.group.dto.GroupDTO;
import com.ebiz.wsb.domain.guardian.application.GuardianService;
import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/guardian")
@RequiredArgsConstructor
public class GuardianController {
    private final GuardianService guardianService;

    @GetMapping
    public ResponseEntity<GuardianDTO> getMyGuardianInfo() {
        GuardianDTO guardianDTO = guardianService.getMyGuardianInfo();
        log.info("Retrieved logged-in Guardian information");
        return new ResponseEntity<>(guardianDTO, HttpStatus.OK);
    }

    @GetMapping("/my-child")
    public ResponseEntity<List<GuardianDTO>> getGuardiansForMyChild() {
        List<GuardianDTO> guardians = guardianService.getGuardiansForMyChild();
        return ResponseEntity.ok(guardians);
    }

    @PutMapping
    public ResponseEntity<GuardianDTO> updateMyGuardianInfo(
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "experience", required = false) String experience,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        GuardianDTO guardianDTO = GuardianDTO.builder()
                .bio(bio)
                .experience(experience)
                .phone(phone)
                .build();

        GuardianDTO updatedGuardian = guardianService.updateMyGuardianInfo(guardianDTO, file);
        return ResponseEntity.ok(updatedGuardian);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteMyGuardianInfo() {
        guardianService.deleteMyGuardianInfo();
        log.info("Deleted logged-in Guardian information");
        return ResponseEntity.ok("정보가 성공적으로 삭제 되었습니다.");
    }

    @GetMapping("/group")
    public ResponseEntity<GroupDTO> getGuardianGroup() {
        GroupDTO group = guardianService.getGuardianGroup();
        return new ResponseEntity<>(group, HttpStatus.OK);
    }
}
