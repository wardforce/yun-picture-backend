package com.wuzhenhua.yunpicturebackend.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.mail")
@Data
public class JavaMailSender {
    private String host;
    private String port;
    private String username;
    private String password;
}
