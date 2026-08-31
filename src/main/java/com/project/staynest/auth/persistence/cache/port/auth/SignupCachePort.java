package com.project.staynest.auth.persistence.cache.port.auth;


import com.project.staynest.auth.persistence.cache.redis.model.signup.SignupCacheKeyData;
import com.project.staynest.auth.persistence.cache.redis.model.signup.SignupCacheSessionData;
import com.project.staynest.auth.persistence.cache.redis.model.signup.SignupCacheData;
import reactor.core.publisher.Mono;

public interface SignupCachePort {

    Mono<Void> saveSignupCache(SignupCacheSessionData signupCacheData);

    Mono<SignupCacheData> getSignupData(SignupCacheKeyData keyData);

}
