package com.project.staynest.auth.otp.service.impl;

import com.project.staynest.auth.persistence.cache.redis.mapper.otp.OtpCacheKeyDataMapper;
import com.project.staynest.auth.persistence.cache.redis.mapper.otp.OtpCacheMetaDataMapper;
import com.project.staynest.auth.persistence.cache.redis.mapper.otp.VerifyOtpCacheDataMapper;
import com.project.staynest.auth.otp.model.VerifyOtpDetails;
import com.project.staynest.auth.otp.service.VerifyOtpService;
import com.project.staynest.auth.errorhandling.exceptions.otp.OtpExpiredException;
import com.project.staynest.auth.errorhandling.exceptions.otp.OtpNotMatchedException;
import com.project.staynest.auth.errorhandling.exceptions.otp.OtpVerifyAttemptsExceededException;
import com.project.staynest.auth.errorhandling.exceptions.otp.OtpVerifyRateLimitedException;
import com.project.staynest.auth.otp.OtpGenerator.OtpGenerator;
import com.project.staynest.auth.persistence.cache.port.otp.OtpCachePort;
import com.project.staynest.auth.validation.Validation;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class VerifyOtpServiceImpl implements VerifyOtpService {

    private final String CLASS_NAME = this.getClass().getSimpleName();
    private final OtpCachePort otpCachePort;
    private final OtpGenerator otpGenerator;
    public VerifyOtpServiceImpl(
            OtpCachePort otpCachePort,
            OtpGenerator otpGenerator
            ){
        this.otpCachePort = otpCachePort;
        this.otpGenerator = otpGenerator;
    }

    @Override
    public Mono<Void> verifyOtp(VerifyOtpDetails verifyOtpDetails) {

        return Mono.defer(()-> {
            Validation.validate(verifyOtpDetails, "verifyOtpDetails", CLASS_NAME);

            return this.otpCachePort.checkOtpValid(
                    OtpCacheMetaDataMapper.from(verifyOtpDetails)
            );

        })

                .filter(valid-> valid)
                .switchIfEmpty(Mono.error(new OtpExpiredException()))

                .then(Mono.defer(()->{

                    return this.otpCachePort.rateLimitVerify(
                            OtpCacheKeyDataMapper.from(
                                    verifyOtpDetails
                            )
                    );
                }))

                .filter(allowed-> allowed)
                .switchIfEmpty(Mono.error(new OtpVerifyRateLimitedException()))

                .then(Mono.defer(()->{

                    String hashedOtp = this.otpGenerator.generateOtpHash(verifyOtpDetails.otp());

                    return this.otpCachePort.verifyOtp(
                            VerifyOtpCacheDataMapper.from(
                                    verifyOtpDetails,
                                    hashedOtp
                            )
                    );
                }))

                .flatMap(verifyOtpStatus ->
                        switch(verifyOtpStatus){

                            case SUCCESS ->
                                    Mono.empty();
                            case OTP_EXPIRED ->
                                    Mono.error(new OtpExpiredException());
                            case ATTEMPTS_EXHAUSTED ->
                                    Mono.error(new OtpVerifyAttemptsExceededException());
                            case OTP_NOT_MATCHED ->
                                    Mono.error(new OtpNotMatchedException());

                        }
                );
    }
}
