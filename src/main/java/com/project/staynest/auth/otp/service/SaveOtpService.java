package com.project.staynest.auth.otp.service;

import com.project.staynest.auth.otp.model.OtpMetadata;
import reactor.core.publisher.Mono;

public interface SaveOtpService {
    Mono<String> saveOtp(OtpMetadata otpData);
}
