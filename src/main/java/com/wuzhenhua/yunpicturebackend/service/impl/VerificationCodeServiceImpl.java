package com.wuzhenhua.yunpicturebackend.service.impl;

import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.service.EmailService;
import com.wuzhenhua.yunpicturebackend.service.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final String CODE_PREFIX = "yun-picture:email:code:";
    private static final String COOLDOWN_PREFIX = "yun-picture:email:cooldown:";
    private static final long CODE_TTL_MINUTES = 5;
    private static final long COOLDOWN_SECONDS = 60;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private EmailService emailService;

    @Override
    public void generateAndSendCode(String email, String codeType) {
        checkCooldown(email);
        String code = generateCode();
        String key = CODE_PREFIX + codeType + ":" + email;
        stringRedisTemplate.opsForValue().set(key, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);
        String cooldownKey = COOLDOWN_PREFIX + email;
        stringRedisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);
        emailService.sendVerificationCode(email, code);
        log.info("验证码已生成并发送: email={}, codeType={}", email, codeType);
    }

    @Override
    public boolean validateCode(String email, String code, String codeType) {
        String key = CODE_PREFIX + codeType + ":" + email;
        String storedCode = stringRedisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码已过期或不存在");
        }
        if (!storedCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        return true;
    }

    @Override
    public void checkCooldown(String email) {
        String cooldownKey = COOLDOWN_PREFIX + email;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "发送过于频繁，请稍后重试");
        }
    }

    @Override
    public void markCodeAsUsed(String email, String codeType) {
        String key = CODE_PREFIX + codeType + ":" + email;
        stringRedisTemplate.delete(key);
    }

    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
