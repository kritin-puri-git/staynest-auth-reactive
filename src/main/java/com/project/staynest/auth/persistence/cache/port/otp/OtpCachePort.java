package com.project.staynest.auth.persistence.cache.port.otp;

import com.project.staynest.auth.otp.enums.ResendOtpStatus;
import com.project.staynest.auth.otp.enums.VerifyOtpStatus;
import com.project.staynest.auth.persistence.cache.redis.model.otp.*;
import reactor.core.publisher.Mono;

public interface OtpCachePort {

    Mono<Void> saveOtp(OtpCacheData otpData);

    Mono<Boolean> checkOtpValid(OtpCacheMetadata cacheDetails);

    Mono<ResendOtpStatus> saveOtpAgain(ResendOtpCacheData resendOtpData);

    Mono<VerifyOtpStatus> verifyOtp(VerifyOtpCacheData verifyOtpData);

    Mono<Boolean> rateLimitVerify(OtpCacheKeyData cacheKeyData);
    Mono<Boolean> rateLimitResend(OtpCacheKeyData cacheKeyData);
}
