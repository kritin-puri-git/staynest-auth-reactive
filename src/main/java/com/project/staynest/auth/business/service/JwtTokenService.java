package com.project.staynest.auth.business.service;

import com.project.staynest.auth.business.model.LatestTokenData;
import com.project.staynest.auth.business.model.jwt.JwtGenerationResult;
import com.project.staynest.auth.business.model.jwt.JwtUserData;
import com.project.staynest.auth.enums.UserStatus;
import reactor.core.publisher.Mono;

public interface JwtTokenService {
    Mono<JwtGenerationResult> generateUserTokenDetails(
            String subject,
            UserStatus status,
            String deviceId,
            String userAgent
    );
    Mono<LatestTokenData> rotateJwt(JwtUserData tokenData);
}
