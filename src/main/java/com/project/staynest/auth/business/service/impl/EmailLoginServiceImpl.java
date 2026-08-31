package com.project.staynest.auth.business.service.impl;

import com.project.staynest.auth.business.constants.OtpBusinessConstants;
import com.project.staynest.auth.business.enums.Purpose;
import com.project.staynest.auth.business.facade.CryptoFacade;
import com.project.staynest.auth.business.mapper.encryption.EncryptionResultMapper;
import com.project.staynest.auth.business.model.SentOtpData;
import com.project.staynest.auth.business.model.SigninData;
import com.project.staynest.auth.business.model.login.*;
import com.project.staynest.auth.business.model.login.LoginByEmailModel;
import com.project.staynest.auth.business.model.login.VerifyLoginByEmailModel;
import com.project.staynest.auth.business.service.EmailLoginService;
import com.project.staynest.auth.business.service.JwtTokenService;
import com.project.staynest.auth.crypto.encryption.model.EncryptionResultMap;
import com.project.staynest.auth.crypto.hashing.model.HashingResult;
import com.project.staynest.auth.crypto.hashing.model.HashingResultMap;
import com.project.staynest.auth.enums.UserStatus;
import com.project.staynest.auth.errorhandling.exceptions.business.authentication.UserNotFoundAfterVerificationException;
import com.project.staynest.auth.errorhandling.exceptions.business.authentication.UserNotFoundException;
import com.project.staynest.auth.otp.model.EmailOtpData;
import com.project.staynest.auth.otp.model.VerifyOtpDetails;
import com.project.staynest.auth.otp.service.EmailOtpService;
import com.project.staynest.auth.otp.service.OtpService;
import com.project.staynest.auth.persistence.db.r2dbc.mapper.EmailIndexDataMapper;
import com.project.staynest.auth.persistence.db.r2dbc.model.UsersCryptoRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.model.UsersLookupRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.model.UsersRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.projection.UserDataProjection;
import com.project.staynest.auth.persistence.db.port.LoginRepositoryPort;
import com.project.staynest.auth.validation.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailLoginServiceImpl implements EmailLoginService {
    private final static Purpose PURPOSE = Purpose.LOGIN;
    private final static String MAP_KEY_USERNAME = "username";
    private final static String MAP_KEY_EMAIL = "identifier";
    private final static String MAP_KEY_PUBLIC_ID = "publicId";
    private final static int OTP_EXPIRY_SECONDS = OtpBusinessConstants.OTP_EXPIRY_SECONDS;
    private final static int OTP_TTL_SECONDS = OtpBusinessConstants.OTP_TTL_SECONDS;

    private final static Logger logger = LoggerFactory.getLogger(EmailLoginServiceImpl.class);

    private final String className = this.getClass().getSimpleName();

    private final CryptoFacade cryptoFacade;
    private final LoginRepositoryPort loginRepositoryPort;
    private final EmailOtpService emailOtpService;
    private final OtpService otpService;
    private final JwtTokenService jwtTokenService;
    public EmailLoginServiceImpl(
            CryptoFacade cryptoFacade,
            LoginRepositoryPort loginRepositoryPort,
            EmailOtpService emailOtpService,
            OtpService otpService,
            JwtTokenService jwtTokenService
    ){
        this.cryptoFacade = cryptoFacade;
        this.loginRepositoryPort = loginRepositoryPort;
        this.emailOtpService = emailOtpService;
        this.otpService = otpService;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Mono<SentOtpData> loginByEmail(LoginByEmailModel loginByEmailModel) {

        return Mono.defer(()->{
                    Validation.validate(loginByEmailModel, "loginByEmailModel", className);
                    return this.verifyUserByEmail(loginByEmailModel.email());
        })
                .then(Mono.defer(()->{
                            String token = UUID.randomUUID().toString();
                            return this.sendOtp(token, loginByEmailModel.email())
                                    .thenReturn(new SentOtpData(loginByEmailModel.email(), token));

                        })
                );
    }

    @Transactional
    @Override
    public Mono<SigninData> verifyLogin(VerifyLoginByEmailModel verifyLoginByEmailModel) {

        return Mono.defer(()->{
                    Validation.validate(verifyLoginByEmailModel, "verifyLoginByEmailModel", className);
                    return this.verifyOtp(verifyLoginByEmailModel.email(), verifyLoginByEmailModel.token(), verifyLoginByEmailModel.otp());
        })

                .then(this.getVerifiedUser(verifyLoginByEmailModel.email()))

                .flatMap(userLoginData ->{

                    Map<String, byte[]> encryptedDataMap = Map.of(
                            MAP_KEY_PUBLIC_ID, userLoginData.encryptedPublicId(),
                            MAP_KEY_USERNAME, userLoginData.encryptedUsername(),
                            MAP_KEY_EMAIL, userLoginData.encryptedEmail()
                    );

                    Map<String, String> plainDataMap = this.cryptoFacade.decryptDataMap(
                            encryptedDataMap,
                            userLoginData.encryptionKeyId(),
                            userLoginData.encryptionVersion()
                    );

                    return this.rotateUserDataCrypto(userLoginData, plainDataMap)
                            .then(Mono.defer(()->{
                                UserStatus status = UserStatus.from(userLoginData.status());
                                return this.jwtTokenService.generateUserTokenDetails(
                                        plainDataMap.get(MAP_KEY_PUBLIC_ID),
                                        status,
                                        verifyLoginByEmailModel.deviceId(),
                                        verifyLoginByEmailModel.userAgent()
                                );
                            }))
                            .map(jwtGenerationResult ->
                                    new SigninData(
                                            jwtGenerationResult,
                                            plainDataMap.get(MAP_KEY_PUBLIC_ID),
                                            plainDataMap.get(MAP_KEY_USERNAME),
                                            plainDataMap.get(MAP_KEY_EMAIL)
                                    )
                            );
                });
    }

    private Mono<Void> verifyUserByEmail(String email){

        return Flux.defer(()->{
            List<HashingResult> hashDataList = this.cryptoFacade.getDetailedHashCandidates(
                    email
            );

            return this.loginRepositoryPort.getUsersEmail(
                    EmailIndexDataMapper.from(
                            hashDataList
                    )
            );
        })
                .switchIfEmpty(Mono.error(new UserNotFoundException()))

                .filter(emailLoginVerificationData->
                        email.equalsIgnoreCase(
                                this.cryptoFacade.decrypt(
                                        EncryptionResultMapper.from(
                                                emailLoginVerificationData
                                        )
                                )
                        )
                )
                .next()
                .switchIfEmpty(Mono.error(new UserNotFoundException()))

                .then();
    }

    private Mono<Void> sendOtp(
            String token,
            String email
    ){
        return Mono.defer(()->
                this.emailOtpService.send(

                        new EmailOtpData(
                                PURPOSE.name(),
                                email,
                                token,
                                OTP_EXPIRY_SECONDS,
                                OTP_TTL_SECONDS
                        )
                )
        );
    }

    private Mono<Void> verifyOtp(
            String identifier,
            String token,
            String otp
    ){
        return Mono.defer(()->
                this.otpService.verifyOtp(

                        new VerifyOtpDetails(
                                PURPOSE.name(),
                                identifier,
                                token,
                                otp,
                                OTP_EXPIRY_SECONDS
                        )
                )
        );
    }

    private Mono<UserDataProjection> getVerifiedUser(String email){

        return Flux.defer(()->{
            List<HashingResult> hashDataList = this.cryptoFacade.getDetailedHashCandidates(
                    email
            );

            return this.loginRepositoryPort.getUsersData(
                    EmailIndexDataMapper.from(
                            hashDataList
                    )
            );
        })

                .switchIfEmpty(Mono.error(new UserNotFoundAfterVerificationException()))


                .filter(userLoginData ->
                        email.equalsIgnoreCase(
                                this.cryptoFacade.decrypt(
                                        EncryptionResultMapper.from(
                                                userLoginData
                                        )
                                )
                        )
                )

                .next()

                .switchIfEmpty(Mono.error(new UserNotFoundAfterVerificationException()))

                .doOnError(UserNotFoundAfterVerificationException.class,
                        exception->
                                logger.error(
                                        "User not found after successful OTP verification",
                                        exception
                                )
                );
    }

    private Mono<Void> rotateUserDataCrypto(UserDataProjection userLoginData, Map<String, String> plainDataMap){

        return Mono.defer(()->{
            UsersRotationData encryptedUserData = this.getLatestEncryptedData(
                    userLoginData, plainDataMap
            ).orElse(null);

            UsersLookupRotationData hashedUserData = this.getLatestHashData(
                    userLoginData, plainDataMap
            ).orElse(null);

            if(encryptedUserData == null && hashedUserData == null ){
                return Mono.empty();
            }

            return this.loginRepositoryPort.rotateUsersCrypto(
                    new UsersCryptoRotationData(
                            userLoginData.userLookupId(),
                            encryptedUserData,
                            hashedUserData
                    )
            );


        });
    }

    private Optional<UsersRotationData> getLatestEncryptedData(UserDataProjection userLoginData, Map<String, String> plainDataMap){
        boolean isEncryptionLatest = this.cryptoFacade.isLatestEncryption(
                userLoginData.encryptionKeyId(),
                userLoginData.encryptionVersion()
        );

        if(isEncryptionLatest){
            return Optional.empty();
        }

        EncryptionResultMap encryptedData = this.cryptoFacade.encryptDataMap(plainDataMap);

        return Optional.of(
                new UsersRotationData(
                        encryptedData.encryptedDataMap().get(MAP_KEY_PUBLIC_ID),
                        encryptedData.encryptedDataMap().get(MAP_KEY_USERNAME),
                        encryptedData.encryptedDataMap().get(MAP_KEY_EMAIL),
                        encryptedData.keyId(),
                        encryptedData.version()
                )
        );

    }

    private Optional<UsersLookupRotationData> getLatestHashData(UserDataProjection userLoginData, Map<String, String> plainDataMap){

        boolean isHashingLatest = this.cryptoFacade.isLatestHashing(
                userLoginData.hashingKeyId(),
                userLoginData.hashingVersion()
        );
        if(isHashingLatest){
            return Optional.empty();
        }

        HashingResultMap hashData = this.cryptoFacade.hashDataMap(
                plainDataMap
        );

        return Optional.of(new UsersLookupRotationData(
                hashData.hashDataMap().get(MAP_KEY_PUBLIC_ID),
                hashData.hashDataMap().get(MAP_KEY_USERNAME),
                hashData.hashDataMap().get(MAP_KEY_EMAIL),
                hashData.keyId(),
                hashData.version()
        ));
    }

}