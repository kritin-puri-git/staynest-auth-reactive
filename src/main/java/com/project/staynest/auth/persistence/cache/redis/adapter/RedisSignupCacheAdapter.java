package com.project.staynest.auth.persistence.cache.redis.adapter;

import com.project.staynest.auth.errorhandling.exceptions.unexpected.UnexpectedIllegalStateException;
import com.project.staynest.auth.persistence.cache.port.auth.SignupCachePort;
import com.project.staynest.auth.persistence.cache.redis.keys.RedisKeys;
import com.project.staynest.auth.persistence.cache.redis.model.signup.SignupCacheKeyData;
import com.project.staynest.auth.persistence.cache.redis.model.signup.SignupCacheSessionData;
import com.project.staynest.auth.persistence.cache.redis.model.signup.SignupCacheData;
import com.project.staynest.auth.validation.Validation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;

@Component
public final class RedisSignupCacheAdapter implements SignupCachePort {

    private final String CLASS_NAME = this.getClass().getSimpleName();

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final RedisScript<Long> saveSignupCacheScript;
    public RedisSignupCacheAdapter(
            @Qualifier("masterReactiveRedisTemplate")
            ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
            @Qualifier("saveSignupCacheScript")
            RedisScript<Long> saveSignupCacheScript
            ){
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.saveSignupCacheScript = saveSignupCacheScript;
    }


    @Override
    public Mono<Void> saveSignupCache(SignupCacheSessionData signupCacheData) {

        return Mono.defer(()->{
            Validation.validate(signupCacheData, "signupCacheData", CLASS_NAME);

            String key = RedisKeys.getSignupKey(
                    signupCacheData.hashedEmail(),
                    signupCacheData.token()
            );

            return reactiveRedisTemplate.execute(
                    saveSignupCacheScript,
                    List.of(key),
                    Base64.getEncoder().encodeToString(signupCacheData.encryptedUsername()),
                    Base64.getEncoder().encodeToString(signupCacheData.encryptedEmail()),
                    signupCacheData.encryptionKeyId(),
                    signupCacheData.encryptionVersion(),
                    signupCacheData.ttl()
            )
                    .next()
                    .switchIfEmpty(Mono.error(new UnexpectedIllegalStateException(
                            "Redis Returned null while saving signup data"
                    )))
                    .flatMap(result->
                            switch(result.intValue()){
                                case -1 ->
                                        Mono.error(new UnexpectedIllegalStateException(
                                                "Couldn't save signup data in Cache. " +
                                                        "Same key already exists with same identifier " +
                                                        "and token(UUID)"
                                        ));
                                case 0 ->
                                        Mono.error(new UnexpectedIllegalStateException(
                                                "Signup data provided to save found corrupted"
                                        ));
                                case 1 ->
                                        Mono.empty();
                                default ->
                                        Mono.error(new UnexpectedIllegalStateException(
                                                "Unknown Lua result: " + result
                                        ));
                            }
                    );

        });
    }

    @Override
    public Mono<SignupCacheData> getSignupData(SignupCacheKeyData keyData) {

        return Mono.defer(()->{
            Validation.validate(keyData, "keyData", CLASS_NAME);

            String key = RedisKeys.getSignupKey(keyData.identifier(), keyData.token());

            return reactiveRedisTemplate
                    .opsForHash()
                    .multiGet(
                            key,
                            List.of(
                                    "encryptedUsername",
                                    "encryptedEmail",
                                    "encryptionKeyId",
                                    "encryptionVersion"
                            )
                    )
                    .switchIfEmpty(Mono.error(new UnexpectedIllegalStateException(
                            "Redis returned null while fetching signup cache data"
                    )))
                    .flatMap(signupCacheDataList->{

                        if(signupCacheDataList.isEmpty()){
                            return Mono.empty();
                        }

                        if(signupCacheDataList.size() != 4){
                            return Mono.error(new UnexpectedIllegalStateException(
                                    "Unexpected Redis response size while fetching signup cache data"
                            ));
                        }

                        if(signupCacheDataList.contains(null)){
                            System.out.println(signupCacheDataList);
                            return Mono.error(new UnexpectedIllegalStateException(
                                    "Corrupted signup cache data found while fetching. Fields found null"
                            ));
                        }

                        Object encryptedUsernameObject = signupCacheDataList.get(0);
                        Object encryptedEmailObject = signupCacheDataList.get(1);
                        Object encryptionKeyIdObject = signupCacheDataList.get(2);
                        Object encryptionVersionObject = signupCacheDataList.get(3);

                        if (!(encryptedUsernameObject instanceof String encodedUsername) ||
                                !(encryptedEmailObject instanceof String encodedEmail)) {
                            return Mono.error(new UnexpectedIllegalStateException(
                                    "Corrupted signup cache data. Invalid data."
                            ));
                        }

                        byte[] encryptedUsername = Base64.getDecoder().decode(encodedUsername);
                        byte[] encryptedEmail = Base64.getDecoder().decode(encodedEmail);

                        if (!(encryptionKeyIdObject instanceof Number encryptionKeyIdNumber) ||
                                !(encryptionVersionObject instanceof Number encryptionVersionNumber)) {
                            return Mono.error(new UnexpectedIllegalStateException(
                                    "Corrupted signup cache data. Invalid numeric data type."
                            ));
                        }

                        short encryptionKeyId = encryptionKeyIdNumber.shortValue();
                        short encryptionVersion = encryptionVersionNumber.shortValue();

                        if(encryptedUsername.length == 0
                                || encryptedEmail.length == 0){
                            return Mono.error(new UnexpectedIllegalStateException(
                                    "Corrupted signup cache data found while fetching. Length of encrypted data bytes is 0"
                            ));
                        }
                        if(encryptionKeyId <= 0
                                || encryptionVersion <= 0){
                            return Mono.error(new UnexpectedIllegalStateException(
                                    "Corrupted signup cache data found while fetching. " +
                                            "EncryptionKeyId or EncryptionVersion must be greater than 0"
                            ));
                        }

                        return Mono.just(new SignupCacheData(
                                encryptedUsername,
                                encryptedEmail,
                                encryptionKeyId,
                                encryptionVersion
                        ));
                    });
        });
    }
}