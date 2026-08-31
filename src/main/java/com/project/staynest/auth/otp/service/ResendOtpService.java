package com.project.staynest.auth.otp.service;

import com.project.staynest.auth.otp.model.OtpMetadata;
import reactor.core.publisher.Mono;

public interface ResendOtpService {
    Mono<String> resendOtp(OtpMetadata otpData);
}
