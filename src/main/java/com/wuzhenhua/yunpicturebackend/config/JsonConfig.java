package com.wuzhenhua.yunpicturebackend.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

/**
 * Spring MVC Json 配置
 */
@JsonComponent
public class JsonConfig {

    /**
     * 添加 Long 转 json 精度丢失的配置
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            module.addDeserializer(Long.class, new LongDeserializer());
            module.addDeserializer(Long.TYPE, new LongDeserializer());
            builder.modules(module);
        };
    }

    /**
     * 自定义 Long 反序列化器，支持前端发送 number 或 string 类型
     */
    private static class LongDeserializer extends JsonDeserializer<Long> {
        @Override
        public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_NUMBER_INT) {
                return p.getLongValue();
            }
            if (p.currentToken() == JsonToken.VALUE_STRING) {
                String value = p.getText().trim();
                if (value.isEmpty()) {
                    return null;
                }
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException e) {
                    throw new InvalidFormatException(p, "无效的Long格式", value, Long.class);
                }
            }
            throw ctxt.wrongTokenException(p, Long.class, JsonToken.VALUE_NUMBER_INT,
                    "Long类型仅支持数字或字符串格式");
        }
    }
}
