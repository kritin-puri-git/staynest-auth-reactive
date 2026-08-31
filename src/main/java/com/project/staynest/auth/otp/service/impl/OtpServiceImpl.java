package com.project.staynest.auth.otp.service.impl;

import com.project.staynest.auth.otp.model.OtpMetadata;
import com.project.staynest.auth.otp.model.VerifyOtpDetails;
import com.project.staynest.auth.otp.service.OtpService;
import com.project.staynest.auth.otp.service.ResendOtpService;
import com.project.staynest.auth.otp.service.SaveOtpService;
import com.project.staynest.auth.otp.service.VerifyOtpService;
import com.project.staynest.auth.validation.Validation;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
public class OtpServiceImpl implements OtpService {

    private final String CLASS_NAME = this.getClass().getSimpleName();

    private final SaveOtpService saveOtpService;
    private final VerifyOtpService verifyOtpService;
    private final ResendOtpService resendOtpService;
    public OtpServiceImpl(
            SaveOtpService saveOtpService,
            VerifyOtpService verifyOtpService,
            ResendOtpService resendOtpService
    ){
        this.saveOtpService = saveOtpService;
        this.verifyOtpService = verifyOtpService;
        this.resendOtpService =resendOtpService;
    }

    @Override
    public Mono<String> saveOtp(OtpMetadata otpMetadata) {

        return Mono.defer(()->{
            Validation.validate(otpMetadata, "otpMetadata", CLASS_NAME);
            return this.saveOtpService.saveOtp(otpMetadata);
        });
    }

    @Override
    public Mono<Void> verifyOtp(VerifyOtpDetails verifyOtpDetails) {
        return Mono.defer(()->{
            Validation.validate(verifyOtpDetails, "verifyOtpDetails", CLASS_NAME);
            return this.verifyOtpService.verifyOtp(verifyOtpDetails);
        });

    }

    @Override
    public Mono<String> resendOtp(OtpMetadata otpMetadata) {
        return Mono.defer(()->{
            Validation.validate(otpMetadata, "otpMetadata", CLASS_NAME);
            return this.resendOtpService.resendOtp(otpMetadata);
        });
    }
}
