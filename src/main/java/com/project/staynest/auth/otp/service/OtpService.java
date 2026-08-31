package com.project.staynest.auth.otp.service;

import com.project.staynest.auth.otp.model.OtpMetadata;
import com.project.staynest.auth.otp.model.VerifyOtpDetails;
import reactor.core.publisher.Mono;

public interface OtpService {
    Mono<String> saveOtp(OtpMetadata otpMetadata);
    Mono<Void> verifyOtp(VerifyOtpDetails verifyOtpDetails);
    Mono<String> resendOtp(OtpMetadata otpMetadata);
}
