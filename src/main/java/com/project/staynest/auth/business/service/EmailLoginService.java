package com.project.staynest.auth.business.service;

import com.project.staynest.auth.business.model.SentOtpData;
import com.project.staynest.auth.business.model.SigninData;
import com.project.staynest.auth.business.model.login.LoginByEmailModel;
import com.project.staynest.auth.business.model.login.VerifyLoginByEmailModel;
import reactor.core.publisher.Mono;

public interface EmailLoginService {

    Mono<SentOtpData> loginByEmail(LoginByEmailModel loginByEmailModel);
    Mono<SigninData> verifyLogin(VerifyLoginByEmailModel verifyLoginByEmailModel);
}
