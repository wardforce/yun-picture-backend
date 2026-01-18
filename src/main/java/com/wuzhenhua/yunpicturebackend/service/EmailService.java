package com.wuzhenhua.yunpicturebackend.service;

public interface EmailService {
    void sendVerificationCode(String toEmail, String code);
}
