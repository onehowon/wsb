package com.ebiz.wsb.domain.token.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TokenService {

    public String resolveToken(String tokenHeader) {
        if (StringUtils.hasText(tokenHeader) && tokenHeader.startsWith("Bearer")) {
            return tokenHeader.substring(7);
        }
        return null;
    }
}
