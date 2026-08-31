package com.project.staynest.auth.otp.service;

import com.project.staynest.auth.otp.model.EmailOtpData;
import reactor.core.publisher.Mono;

public interface EmailOtpService {
    Mono<Void> send(EmailOtpData emailOtpData);
    Mono<Void> resend(EmailOtpData emailOtpData);
}
