package com.wuzhenhua.yunpicturebackend.api.gemini;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.HttpOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Gemini {
        @Value("${langchain4j.google-ai-gemini.chat-model.api-key}")
        String ApiKey;

        @Value("${langchain4j.google-ai-gemini.chat-model.base-url}")
        String baseUrl;

        @Value("${langchain4j.google-ai-gemini.chat-model.model-name}")
        public String modelName;

        public HttpOptions httpOptions;
        public Client client;
        public GenerateContentConfig config;

        @PostConstruct
        public void init() {

                httpOptions = HttpOptions.builder()
                                .baseUrl(baseUrl)
                                .build();

                // SDK 要求提供 apiKey，虽然不会用到（我们用拦截器添加到 URL）
                client = Client.builder()
                                .apiKey(ApiKey)
                                .httpOptions(httpOptions)
                                .build();

                log.info("Gemini client initialized successfully with model: {}, baseUrl: {}", modelName, baseUrl);
        }
}
