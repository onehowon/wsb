package com.ebiz.wsb.controller;

import com.ebiz.wsb.dto.UserDTO;
import com.ebiz.wsb.model.User;
import com.ebiz.wsb.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 회원가입 처리 메서드
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            User user = new User();
            // DTO에서 User 엔티티로 데이터 매핑
            user.setUsername(userDTO.getUsername());
            user.setPassword(userDTO.getPassword());
            user.setEmail(userDTO.getEmail());
            user.setPhoneNumber(userDTO.getPhoneNumber());
            user.setUserType(userDTO.getUserType()); // 사용자 유형 설정
            // UserService를 통해 사용자 저장
            User savedUser = userService.saveUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            // 예외 발생 시 오류 메시지 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입에 실패했습니다.: " + e.getMessage());
        }
    }

    // 사용자 이름으로 사용자 정보 조회 메서드
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            // 사용자를 찾지 못한 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }
    }

    // 로그인 처리 메서드
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDTO loginRequest) {
        Optional<User> user = userService.findByUsername(loginRequest.getUsername());
        if (user.isPresent() && userService.checkPassword(loginRequest.getPassword(), user.get().getPassword())) {
            return ResponseEntity.ok("로그인 성공");
        } else {
            // 로그인 실패 시 오류 메시지 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 아이디 또는 패스워드");
        }
    }

    // 비밀번호 변경 처리 메서드
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam String username, @RequestParam String oldPassword, @RequestParam String newPassword) {
        try {
            // UserService를 통해 비밀번호 변경 처리
            userService.changePassword(username, oldPassword, newPassword);
            return ResponseEntity.ok("비밀번호 변경 성공");
        } catch (Exception e) {
            // 예외 발생 시 오류 메시지 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호 변경 실패: " + e.getMessage());
        }
    }
}
