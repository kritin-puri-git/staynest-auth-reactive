package com.project.staynest.auth.otp.service.impl;

import com.project.staynest.auth.email.models.SendOtpByEmailEvent;
import com.project.staynest.auth.email.port.SendOtpByEmailPort;
import com.project.staynest.auth.otp.mapper.OtpMetaDataMapper;
import com.project.staynest.auth.otp.model.EmailOtpData;
import com.project.staynest.auth.otp.service.*;
import com.project.staynest.auth.otp.service.EmailOtpService;
import com.project.staynest.auth.otp.service.OtpService;
import com.project.staynest.auth.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class EmailOtpServiceImpl implements EmailOtpService {

    private final String CLASS_NAME = this.getClass().getSimpleName();

    private final OtpService otpService;
    private final SendOtpByEmailPort otpMail;
    private final Validator validator;
    public EmailOtpServiceImpl(
            OtpService otpService,
            SendOtpByEmailPort otpMail,
            Validator validator
    ){
        this.otpService = otpService;
        this.otpMail = otpMail;
        this.validator = validator;
    }
    @Override
    public Mono<Void> send(EmailOtpData emailOtpData) {
        return Mono.defer(()->{
            Validation.validate(emailOtpData, "emailOtpData", CLASS_NAME);
            if(!validator.validate(emailOtpData).isEmpty()){
                throw new IllegalArgumentException(
                        "Invalid Email found in EmailOtpService while sending otp"
                );
            }

            return this.otpService.saveOtp(OtpMetaDataMapper.from(emailOtpData));
        })
                .flatMap(otp->
                        sendEmail(
                                emailOtpData.email(),
                                otp,
                                emailOtpData.purpose()
                        )
                );
    }
    @Override
    public Mono<Void> resend(EmailOtpData emailOtpData) {

        return Mono.defer(()->{
            Validation.validate(emailOtpData, "emailOtpData", CLASS_NAME);
            if(!validator.validate(emailOtpData).isEmpty()){
                throw new IllegalArgumentException(
                        "Invalid Email found in EmailOtpService while resending otp"
                );
            }

            return this.otpService.resendOtp(OtpMetaDataMapper.from(emailOtpData));

        })
                .flatMap(otp->
                        sendEmail(
                                emailOtpData.email(),
                                otp,
                                emailOtpData.purpose()
                        )
                );
    }


    private Mono<Void> sendEmail(
            String email,
            String otp,
            String purpose
    ){
        return Mono.defer(()->{
            Validation.validate(email, "identifier", CLASS_NAME);
            Validation.validate(otp, "otp", CLASS_NAME);
            Validation.validate(purpose, "purpose", CLASS_NAME);

            return Mono.fromRunnable(()->
                    this.otpMail.sendOtpByEmail(
                            new SendOtpByEmailEvent(
                                    email,
                                    otp,
                                    purpose
                            )
                    )
            ).subscribeOn(Schedulers.boundedElastic());
        })
                .then();
    }
}
