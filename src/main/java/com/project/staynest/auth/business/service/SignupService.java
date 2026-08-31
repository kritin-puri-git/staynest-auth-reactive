package com.project.staynest.auth.business.service;

import com.project.staynest.auth.business.model.SentOtpData;
import com.project.staynest.auth.business.model.SigninData;
import com.project.staynest.auth.business.model.signup.SignupModel;
import com.project.staynest.auth.business.model.signup.VerifySignupModel;
import reactor.core.publisher.Mono;

public interface SignupService {

    Mono<SentOtpData> signup(SignupModel signupModel);
    Mono<SigninData> verifySignup(VerifySignupModel verifySignupModel);
}
