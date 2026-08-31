package com.project.staynest.auth.business.service.impl;

import com.project.staynest.auth.business.constants.OtpBusinessConstants;
import com.project.staynest.auth.business.model.SigninData;
import com.project.staynest.auth.business.model.signup.SignupModel;
import com.project.staynest.auth.business.model.signup.VerifySignupModel;
import com.project.staynest.auth.business.service.JwtTokenService;
import com.project.staynest.auth.enums.UserStatus;
import com.project.staynest.auth.business.facade.CryptoFacade;
import com.project.staynest.auth.business.model.SentOtpData;
import com.project.staynest.auth.business.model.signup.*;
import com.project.staynest.auth.business.service.SignupService;

import com.project.staynest.auth.business.enums.Purpose;
import com.project.staynest.auth.business.constants.SignupBusinessConstants;

import com.project.staynest.auth.crypto.encryption.model.EncryptionResultMap;
import com.project.staynest.auth.crypto.hashing.model.HashingResultMap;
import com.project.staynest.auth.errorhandling.exceptions.business.authentication.SignupCacheExpiredException;
import com.project.staynest.auth.errorhandling.exceptions.unexpected.UnexpectedIllegalStateException;
import com.project.staynest.auth.errorhandling.exceptions.business.validation.EmailAlreadyExistsException;
import com.project.staynest.auth.errorhandling.exceptions.business.validation.UsernameAlreadyExistsException;


import com.project.staynest.auth.otp.model.EmailOtpData;
import com.project.staynest.auth.otp.model.VerifyOtpDetails;
import com.project.staynest.auth.otp.service.EmailOtpService;
import com.project.staynest.auth.otp.service.OtpService;

import com.project.staynest.auth.persistence.cache.port.auth.SignupCachePort;

import com.project.staynest.auth.persistence.cache.redis.model.signup.SignupCacheData;
import com.project.staynest.auth.persistence.cache.redis.model.signup.SignupCacheKeyData;
import com.project.staynest.auth.persistence.cache.redis.model.signup.SignupCacheSessionData;
import com.project.staynest.auth.persistence.db.r2dbc.model.UserData;
import com.project.staynest.auth.persistence.db.port.SignupRepositoryPort;
import com.project.staynest.auth.validation.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class SignupServiceImpl implements SignupService {
    private final static String MAP_KEY_USERNAME = "username";
    private final static String MAP_KEY_EMAIL = "identifier";
    private final static String MAP_KEY_PUBLIC_ID = "publicId";
    private final static Purpose PURPOSE = Purpose.SIGNUP;
    private final static int SIGNUP_CACHE_EXPIRY_SECONDS = SignupBusinessConstants.SIGN_UP_CACHE_EXPIRY_SECONDS;
    private final static int SIGNUP_CACHE_TTL_SECONDS = SignupBusinessConstants.SIGN_UP_CACHE_TTL_SECONDS;
    private final static int OTP_EXPIRY_SECONDS = OtpBusinessConstants.OTP_EXPIRY_SECONDS;
    private final static int OTP_TTL_SECONDS = OtpBusinessConstants.OTP_TTL_SECONDS;

    private final static Logger logger = LoggerFactory.getLogger(SignupServiceImpl.class);

    private final String CLASS_NAME = this.getClass().getSimpleName();

    private final SignupCachePort signupCachePort;
    private final CryptoFacade cryptoFacade;
    private final EmailOtpService emailOtpService;
    private final OtpService otpService;
    private final SignupRepositoryPort signupRepositoryPort;
    private final JwtTokenService jwtTokenService;
    public SignupServiceImpl(
            SignupCachePort signupCachePort,
            CryptoFacade cryptoFacade,
            EmailOtpService emailOtpService,
            OtpService otpService,
            SignupRepositoryPort signupRepositoryPort,
            JwtTokenService jwtTokenService
    ){
        this.signupCachePort = signupCachePort;
        this.cryptoFacade = cryptoFacade;
        this.emailOtpService = emailOtpService;
        this.otpService = otpService;
        this.signupRepositoryPort = signupRepositoryPort;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Mono<SentOtpData> signup(SignupModel signupModel) {

        return Mono.defer(()->{
            Validation.validate(signupModel, "signupModel", CLASS_NAME);
            return checkUserExists(signupModel.email(), signupModel.username());
        })
                .then(Mono.defer(()-> {

                            String token = UUID.randomUUID().toString();

                            return saveSignupCache(signupModel, token)
                                    .then(this.sendOtp(signupModel.email(), token))
                                    .thenReturn(new SentOtpData(signupModel.email(), token));
                        })
                );
    }

    @Transactional
    @Override
    public Mono<SigninData> verifySignup(VerifySignupModel verifySignupModel) {

        return Mono.defer(() ->{
            Validation.validate(verifySignupModel, "verifySignupModel", CLASS_NAME);
            return this.verifyOtp(verifySignupModel.email(), verifySignupModel.token(), verifySignupModel.otp());
        })

                .then(this.getSignupCache(verifySignupModel))

                .flatMap(signupData->{
                    Validation.validate(signupData, "signupData", CLASS_NAME);

                    Map<String, String> decryptedSignupDataMap = this.cryptoFacade.decryptDataMap(
                            Map.of(
                                    MAP_KEY_USERNAME, signupData.encryptedUsername(),
                                    MAP_KEY_EMAIL, signupData.encryptedEmail()
                            ),
                            signupData.encryptionKeyId(),
                            signupData.encryptionVersion()
                    );
                    return this.saveUser(
                            decryptedSignupDataMap.get(
                                    MAP_KEY_USERNAME
                            ),
                            decryptedSignupDataMap.get(
                                    MAP_KEY_EMAIL
                            )
                    )
                            .flatMap(publicId->
                                    this.jwtTokenService.generateUserTokenDetails(
                                            publicId,
                                            UserStatus.ACTIVE,
                                            verifySignupModel.deviceId(),
                                            verifySignupModel.userAgent()
                                    )
                                            .map(jwtGenerationResult ->
                                                    new SigninData(
                                                            jwtGenerationResult,
                                                            publicId,
                                                            decryptedSignupDataMap.get(MAP_KEY_USERNAME),
                                                            decryptedSignupDataMap.get(MAP_KEY_EMAIL)
                                                    )
                                            )
                            );

                });
    }

    private Mono<Void> checkUserExists(String email, String username){

        return checkEmailExists(email)
                .then(
                        checkUsernameExists(username)
                );
    }

    private Mono<Void> checkEmailExists(String email){

        return Mono.defer(()->{
            Validation.validate(email, "identifier", CLASS_NAME);
            return this.signupRepositoryPort.checkEmailIndexExists(
                    this.cryptoFacade.getHashCandidates(email)
            );
        })
                .flatMap(emailExists ->{

                    if(emailExists){
                        return Mono.error(new EmailAlreadyExistsException());
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> checkUsernameExists(String username){

        return Mono.defer(()->{
            Validation.validate(username, "username", CLASS_NAME);
            return this.signupRepositoryPort.checkUsernameIndexExists(
                    this.cryptoFacade.getHashCandidates(username)
            );
        })
                .flatMap(usernameExists->{
                    if(usernameExists){
                        return Mono.error(new UsernameAlreadyExistsException());
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> saveSignupCache(
            SignupModel signupModel,
            String token
    ){
        return Mono.defer(()->{
            Map<String, String> signupData = Map.of(
                    MAP_KEY_USERNAME, signupModel.username(),
                    MAP_KEY_EMAIL, signupModel.email()
            );
            EncryptionResultMap encryptedDataMap = this.cryptoFacade.encryptDataMap(
                    signupData
            );

            byte[] identifierHash = this.cryptoFacade.hashValue(signupModel.email());
            String hashedIdentifier = Base64.getEncoder()
                    .withoutPadding()
                    .encodeToString(identifierHash);

            SignupCacheSessionData signupCacheDetails = new SignupCacheSessionData(
                    hashedIdentifier,
                    token,
                    encryptedDataMap.encryptedDataMap().get(MAP_KEY_USERNAME),
                    encryptedDataMap.encryptedDataMap().get(MAP_KEY_EMAIL),
                    encryptedDataMap.keyId(),
                    encryptedDataMap.version(),
                    SIGNUP_CACHE_TTL_SECONDS
            );

            return this.signupCachePort.saveSignupCache(
                    signupCacheDetails
            );

        });
    }

    private Mono<Void> sendOtp(
            String email,
            String token
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

    private Mono<SignupCacheData> getSignupCache(
            VerifySignupModel verifySignupModel
    ){

        return Mono.defer(()->{
            byte[] identifierHash = this.cryptoFacade.hashValue(verifySignupModel.email());
            String hashedIdentifier = Base64.getEncoder()
                    .withoutPadding()
                    .encodeToString(identifierHash);

            SignupCacheKeyData keyDetails = new SignupCacheKeyData(
                    hashedIdentifier,
                    verifySignupModel.token()
            );

            return this.signupCachePort.getSignupData(
                            keyDetails
                    )
                    .map(signupData->{
                        Validation.validate(signupData, "signupData", CLASS_NAME);
                        return signupData;
                    });
        })
                .doOnError(IllegalArgumentException.class, exception->
                        logger.error(
                                "Signup cache missing after successful OTP verification. " +
                                        "Possible Redis anomaly or premature cache expiration.",
                                exception
                        )
                )
                .onErrorMap(IllegalArgumentException.class, _ ->
                        new SignupCacheExpiredException()
                );
    }

    private Mono<String> getPublicId(){

        return Flux.range(1,5)

                .concatMap(ignored->{
                    String publicId = UUID.randomUUID().toString();

                    return this.signupRepositoryPort.checkPublicIdIndexExists(
                                    this.cryptoFacade.getHashCandidates(publicId)
                            )

                            .flatMap(publicIdExists->
                                    publicIdExists
                                            ?Mono.empty()
                                            :Mono.just(publicId)
                            );

                })

                .next()

                .switchIfEmpty(Mono.error(new UnexpectedIllegalStateException("Unable to generate unique Public Id")))
                .doOnError(UnexpectedIllegalStateException.class,
                        _ ->
                        logger.warn(
                                "Unable to generate unique Public Id. " +
                                        "5 Different Public Ids(UUID) generated but every id already existed"
                        )
                );
    }

    private Mono<String> saveUser(final String decryptedUsername, final String decryptedEmail){

        return Mono.defer(()->
                this.checkUserExists(decryptedEmail, decryptedUsername)
                .then(this.getPublicId())
                .flatMap(publicId->{
                    Map<String, String> signupDataMap = Map.of(
                            MAP_KEY_PUBLIC_ID, publicId,
                            MAP_KEY_USERNAME, decryptedUsername,
                            MAP_KEY_EMAIL, decryptedEmail
                    );
                    EncryptionResultMap encryptedDataMap = this.cryptoFacade.encryptDataMap(
                            signupDataMap
                    );

                    HashingResultMap hashDataMap = this.cryptoFacade.hashDataMap(
                            signupDataMap
                    );

                    UserData signupUserData = new UserData(
                            encryptedDataMap.encryptedDataMap().get(MAP_KEY_PUBLIC_ID),
                            encryptedDataMap.encryptedDataMap().get(MAP_KEY_USERNAME),
                            encryptedDataMap.encryptedDataMap().get(MAP_KEY_EMAIL),
                            encryptedDataMap.keyId(),
                            encryptedDataMap.version(),
                            hashDataMap.hashDataMap().get(MAP_KEY_PUBLIC_ID),
                            hashDataMap.hashDataMap().get(MAP_KEY_USERNAME),
                            hashDataMap.hashDataMap().get(MAP_KEY_EMAIL),
                            hashDataMap.keyId(),
                            hashDataMap.version(),
                            UserStatus.ACTIVE.name()

                    );

                    return this.signupRepositoryPort.saveUserData(
                                    signupUserData
                    )
                            .thenReturn(publicId);
                }));
    }
}
