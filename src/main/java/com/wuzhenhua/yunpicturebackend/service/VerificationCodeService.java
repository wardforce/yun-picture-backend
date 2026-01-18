package com.wuzhenhua.yunpicturebackend.service;

public interface VerificationCodeService {
    void generateAndSendCode(String email, String codeType);

    boolean validateCode(String email, String code, String codeType);

    void checkCooldown(String email);

    void markCodeAsUsed(String email, String codeType);
}
