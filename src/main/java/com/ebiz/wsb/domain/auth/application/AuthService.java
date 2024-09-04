package com.ebiz.wsb.domain.auth.application;

import com.ebiz.wsb.domain.auth.dto.SignInDTO;
import com.ebiz.wsb.domain.auth.dto.SignInRequest;
import com.ebiz.wsb.domain.auth.dto.SignUpRequest;
import com.ebiz.wsb.domain.auth.dto.UserType;
import com.ebiz.wsb.domain.auth.exception.DuplicatedSignUpException;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.parent.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtProvider jwtProvider;
    private final GuardianRepository guardianRepository;
    private final ParentRepository parentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(SignUpRequest request){
        try {
            if(request.getUserType() == UserType.GUARDIAN){
                Guardian guardian = Guardian.builder()
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .name(request.getName())
                        .build();
                guardianRepository.save(guardian);
            } else if(request.getUserType() == UserType.PARENT){
                Parent parent = Parent.builder()
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .name(request.getName())
                        .build();
                parentRepository.save(parent);
            } else {
                throw new IllegalArgumentException("유효하지 않은 유저 타입입니다.");
            }
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatedSignUpException("이미 존재하는 이메일입니다.");
        }
    }


    public SignInDTO signIn(SignInRequest request){
        Authentication authentication = authenticate(request);
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        Object principal = authentication.getPrincipal();
        Long userId = null;
        String message = "로그인 성공";

        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();

            // Guardian 또는 Parent에서 사용자 찾기
            if (authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_GUARDIAN"))) {
                Guardian guardian = guardianRepository.findGuardianByEmail(email)
                        .orElse(null);
                if (guardian != null) {
                    userId = guardian.getId();
                }
            } else if (authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_PARENT"))) {
                Parent parent = parentRepository.findParentByEmail(email)
                        .orElse(null);
                if (parent != null) {
                    userId = parent.getId();
                }
            }
        }

        return SignInDTO.builder()
                .id(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .message(message)
                .build();
    }

    private Authentication authenticate(SignInRequest request) {
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        );
        return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    }
}