package com.wuzhenhua.yunpicturebackend.api.gemini;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.HttpOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
@Slf4j
@Component
public class Gemini {
        @Value("${langchain4j.google-ai-gemini.chat-model.api-key}")
        String ApiKey;
        
        @Value("${langchain4j.google-ai-gemini.chat-model.base-url}")
        String baseUrl;

        @Value("${langchain4j.google-ai-gemini.chat-model.model-name}")
        public String modelName;
        
        HttpOptions httpOptions = HttpOptions.builder()
                        .baseUrl(baseUrl)
                        .build();
        
        public Client client = Client.builder()
                        .apiKey(ApiKey)
                        .httpOptions(httpOptions)
                        .build();

        List<String> responseModalities = List.of("TEXT", "IMAGE");
        GenerateContentConfig config = GenerateContentConfig.builder()
                        .responseModalities(responseModalities)
                        .build();
}
