package com.wuzhenhua.yunpicturebackend.api.gemini.service.Impl;

import autovalue.shaded.com.google.common.collect.ImmutableList;
import com.google.genai.types.*;
import com.wuzhenhua.yunpicturebackend.api.gemini.Gemini;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.AiGenerateResponse;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.CreateChatRequest;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.CreateImageRequest;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.ImageResponse;
import com.wuzhenhua.yunpicturebackend.api.gemini.service.AiPictureGeneratorService;
import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.model.dto.picture.PictureUploadRequest;
import com.wuzhenhua.yunpicturebackend.model.entity.ChatHistory;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.vo.PictureVO;
import com.wuzhenhua.yunpicturebackend.service.ChatHistoryService;
import com.wuzhenhua.yunpicturebackend.service.PictureService;
import com.wuzhenhua.yunpicturebackend.service.UserService;
import com.wuzhenhua.yunpicturebackend.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AiPictureGeneratorServiceImpl implements AiPictureGeneratorService {

    @Resource
    Gemini gemini;
    @Resource
    UserService userService;
    @Resource
    PictureService pictureService;
    @Resource
    ChatHistoryService chatHistoryService;

    @Override
    public ImageResponse generateImages(CreateImageRequest request, HttpServletRequest httpServletRequest) {
        // 1.检验用户
        User loginUser = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户不能为空");
        String prompt = request.getPrompt();
        log.info("Prompt: {}", prompt);
        // 判断图片
        MultipartFile file = request.getFile();
        ImageResponse imageResponse = new ImageResponse();

        // 配置 Gemini 生成内容
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities(ImmutableList.of("IMAGE", "TEXT"))
                .build();

        try {
            List<Content> contents;
            if (file == null) {
                // 仅文字生成图片
                log.info("发送纯文本请求到 Gemini API, prompt length: {}", prompt.length());
                contents = ImmutableList.of(Content.builder()
                        .role("user")
                        .parts(ImmutableList.of(Part.fromText(prompt)))
                        .build());
            } else {
                // 图片+文字生成新图片
                // 1. 先上传到 COS，获取压缩后的图片
                PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                PictureVO uploadPicture = pictureService.uploadPicture(
                        file, pictureUploadRequest, loginUser);
                imageResponse.setUploadPicture(uploadPicture);

                // 2. 从 thumbnailUrl 下载压缩后的图片发送给 Gemini
                String thumbnailUrl = uploadPicture.getThumbnailUrl();
                byte[] compressedImageBytes = downloadImageFromUrl(thumbnailUrl);

                log.info("发送图片+文本请求到 Gemini API, 原始大小: {} bytes, 压缩后: {} bytes, prompt length: {}",
                        file.getSize(), compressedImageBytes.length, prompt.length());

                // 3. 构建包含图片和文字的请求内容
                contents = ImmutableList.of(Content.builder()
                        .role("user")
                        .parts(ImmutableList.of(
                                Part.fromText(prompt),
                                Part.fromBytes(compressedImageBytes, "image/webp")))
                        .build());
            }

            // 调用 Gemini API（使用非流式接口，避免 SDK 流式解析 bug）
            GenerateContentResponse response = gemini.client.models.generateContent(gemini.modelName, contents, config);

            // 处理 Gemini 返回的响应
            if (response.candidates().isPresent() && !response.candidates().get().isEmpty()) {
                List<Part> parts = response.candidates().get().get(0).content().get().parts().get();
                for (Part part : parts) {
                    if (part.inlineData().isPresent()) {
                        // 处理图片数据
                        Blob inlineData = part.inlineData().get();
                        if (inlineData.data().isPresent()) {
                            byte[] generatedImageBytes = inlineData.data().get();
                            String generatedMimeType = inlineData.mimeType().orElse("image/png");
                            String fileName = "ai_generated_" + UUID.randomUUID()
                                    + getExtensionFromMimeType(generatedMimeType);

                            log.info("收到 Gemini 生成的图片, size: {} bytes, mimeType: {}",
                                    generatedImageBytes.length, generatedMimeType);

                            // 创建 MultipartFile 并上传到 COS
                            MultipartFile generatedFile = new Base64DecodedMultipartFile(
                                    generatedImageBytes, fileName, generatedMimeType);

                            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                            PictureVO generatePicture = pictureService.uploadPicture(
                                    generatedFile, pictureUploadRequest, loginUser);
                            imageResponse.setGeneratePicture(generatePicture);
                        }
                    } else if (part.text().isPresent()) {
                        // 处理文本数据
                        String text = part.text().get();
                        log.info("收到 Gemini 文本响应: {}", text);
                        imageResponse.setText(text);
                    }
                }
            }

            log.info("Gemini API 响应处理完成");
            return imageResponse;

        } catch (IOException e) {
            log.error("图片处理失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片处理失败:" + e.getMessage());
        } catch (Exception e) {
            log.error("Gemini API 调用失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Gemini API 调用失败:" + e.getMessage());
        }
    }


    @Override
    public AiGenerateResponse generateAiImage(CreateChatRequest request, HttpServletRequest httpServletRequest) {
        // 1. 验证用户
        User loginUser = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户不能为空");
        String prompt = request.getPrompt();
        Long pictureId = request.getPictureId();

        Long sessionId = request.getSessionId();

        //  2.保存用户消息到 chat_history
        ChatHistory userChatHistory = chatHistoryService.saveUserMessage(
                loginUser.getId(), prompt, pictureId, request.getSessionId());
        log.info("prepareImage - 用户消息已保存, chatHistoryId: {}", userChatHistory.getId());


        AiGenerateResponse response = new AiGenerateResponse();

        // 配置 Gemini 生成内容
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities(ImmutableList.of("IMAGE", "TEXT"))
                .build();

        try {
            List<Content> contents;

            if ( pictureId == null) {
                // 仅文字生成图片
                log.info("generateAiImage - 发送纯文本请求到 Gemini API");
                contents = ImmutableList.of(Content.builder()
                        .role("user")
                        .parts(ImmutableList.of(Part.fromText(prompt)))
                        .build());
            } else {
                // 图片+文字生成新图片
                PictureVO uploadPicture = pictureService.getPictureVO(
                        pictureService.getById(pictureId), httpServletRequest);
                String thumbnailUrl = uploadPicture.getThumbnailUrl();
                byte[] compressedImageBytes = downloadImageFromUrl(thumbnailUrl);

                log.info("generateAiImage - 发送图片+文本请求到 Gemini API, 图片大小: {} bytes",
                        compressedImageBytes.length);

                contents = ImmutableList.of(Content.builder()
                        .role("user")
                        .parts(ImmutableList.of(
                                Part.fromText(prompt),
                                Part.fromBytes(compressedImageBytes, "image/webp")))
                        .build());
            }

            // 调用 Gemini API
            GenerateContentResponse geminiResponse = gemini.client.models.generateContent(
                    gemini.modelName, contents, config);

            String aiText = null;
            PictureVO generatePicture = null;

            // 处理 Gemini 返回的响应
            if (geminiResponse.candidates().isPresent() && !geminiResponse.candidates().get().isEmpty()) {
                List<Part> parts = geminiResponse.candidates().get().get(0).content().get().parts().get();
                for (Part part : parts) {
                    if (part.inlineData().isPresent()) {
                        Blob inlineData = part.inlineData().get();
                        if (inlineData.data().isPresent()) {
                            byte[] generatedImageBytes = inlineData.data().get();
                            String generatedMimeType = inlineData.mimeType().orElse("image/png");
                            String fileName = "ai_generated_" + UUID.randomUUID()
                                    + getExtensionFromMimeType(generatedMimeType);

                            log.info("generateAiImage - 收到 Gemini 生成的图片, size: {} bytes",
                                    generatedImageBytes.length);

                            MultipartFile generatedFile = new Base64DecodedMultipartFile(
                                    generatedImageBytes, fileName, generatedMimeType);

                            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                            generatePicture = pictureService.uploadPicture(
                                    generatedFile, pictureUploadRequest, loginUser);
                            response.setPictureVO(generatePicture);
                        }
                    } else if (part.text().isPresent()) {
                        aiText = part.text().get();
                        log.info("generateAiImage - 收到 Gemini 文本响应: {}", aiText);
                    }
                }
            }

            // 保存 AI 消息到 chat_history
            Long generatedPictureId = generatePicture != null ? generatePicture.getId() : null;
            // 从用户消息中提取 sessionId，保持同一对话
            Long UserSessionId = userChatHistory.getSessionId();
            ChatHistory aiChatHistory = chatHistoryService.saveAiMessage(
                    loginUser.getId(), aiText != null ? aiText : "", generatedPictureId, UserSessionId);
            response.setChatHistory(aiChatHistory);

            log.info("generateAiImage - AI 消息已保存, chatHistoryId: {}", aiChatHistory.getId());
            return response;

        } catch (IOException e) {
            log.error("图片处理失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片处理失败:" + e.getMessage());
        } catch (Exception e) {
            log.error("Gemini API 调用失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Gemini API 调用失败:" + e.getMessage());
        }
    }

    /**
     * 根据 MIME 类型获取文件扩展名
     */
    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return ".png";
        }
        return switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/bmp" -> ".bmp";
            default -> ".png";
        };
    }

    /**
     * 从 URL 下载图片
     */
    private byte[] downloadImageFromUrl(String imageUrl) throws IOException {
        try (InputStream in = new URL(imageUrl).openStream()) {
            return in.readAllBytes();
        }
    }

    /**
     * 将字节数组转换为 MultipartFile 的实现类
     * 用于将 Gemini 返回的图片数据转换为可上传到 COS 的格式
     */
    private static class Base64DecodedMultipartFile implements MultipartFile {

        private final byte[] content;
        private final String fileName;
        private final String contentType;

        public Base64DecodedMultipartFile(byte[] content, String fileName, String contentType) {
            this.content = content;
            this.fileName = fileName;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return fileName;
        }

        @Override
        public String getOriginalFilename() {
            return fileName;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }
}
