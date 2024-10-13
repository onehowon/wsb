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

@RestController
@Slf4j
@RequestMapping("/guardian")
@RequiredArgsConstructor
public class GuardianController {
    private final GuardianService guardianService;

    @GetMapping("/{id}")
    public ResponseEntity<GuardianDTO> getGuardianById(@PathVariable Long id){
        GuardianDTO guardianDTO = guardianService.getGuardianById(id);
        return new ResponseEntity<>(guardianDTO, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GuardianDTO> updateGuardian(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("bio") String bio,
            @RequestParam("experience") String experience,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {


        GuardianDTO guardianDTO = GuardianDTO.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .bio(bio)
                .experience(experience)
                .build();

        GuardianDTO updatedGuardian = guardianService.updateGuardian(id, guardianDTO, file);
        return new ResponseEntity<>(updatedGuardian, HttpStatus.OK);
    }


    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteGuardian(@PathVariable Long id){
        guardianService.deleteGuardian(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/group")
    public ResponseEntity<GroupDTO> getGuardianGroup() {
        GroupDTO group = guardianService.getGuardianGroup();
        return new ResponseEntity<>(group, HttpStatus.OK);
    }
}
