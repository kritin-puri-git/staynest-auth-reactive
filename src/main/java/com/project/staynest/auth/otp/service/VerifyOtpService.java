package com.project.staynest.auth.otp.service;

import com.project.staynest.auth.otp.model.VerifyOtpDetails;
import reactor.core.publisher.Mono;

public interface VerifyOtpService {
    Mono<Void> verifyOtp(VerifyOtpDetails verifyOtpDetails);
}
