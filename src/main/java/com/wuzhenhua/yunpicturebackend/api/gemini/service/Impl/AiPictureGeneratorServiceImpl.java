package com.wuzhenhua.yunpicturebackend.api.gemini.service.Impl;

import com.google.genai.Chat;
import com.google.genai.types.*;
import com.wuzhenhua.yunpicturebackend.api.gemini.Gemini;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.CreateImageRequest;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.ImageResponse;
import com.wuzhenhua.yunpicturebackend.api.gemini.service.AiPictureGeneratorService;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.model.dto.picture.PictureUploadRequest;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.vo.PictureVO;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;
@Service
@Slf4j
public class AiPictureGeneratorServiceImpl implements AiPictureGeneratorService {

    Gemini gemini;
    @Resource
    UserService userService;
    @Resource
    PictureService pictureService;

    @Override
    public ImageResponse generateImages(CreateImageRequest request, HttpServletRequest httpServletRequest) {
        // 1.检验用户
        User loginUser = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户不能为空");
        String prompt = request.getPrompt();
        // 判断图片
        MultipartFile file = request.getFile();
        ImageResponse imageResponse = new ImageResponse();

        // 配置 Gemini 生成内容
        List<String> responseModalities = List.of("TEXT", "IMAGE");
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities(responseModalities)
                .build();

        try {
            // 使用 Chat 对话模式，方便后续扩展为多轮对话
            Chat chat = gemini.client.chats.create(gemini.modelName, config);
            GenerateContentResponse response;

            if (file == null) {
                // 仅文字生成图片
                response = chat.sendMessage(prompt);
            } else {
                // 图片+文字生成新图片
                // 1. 将 MultipartFile 转为字节数组发送给 Gemini
                byte[] imageBytes = file.getBytes();
                String mimeType = file.getContentType();
                if (mimeType == null) {
                    mimeType = "image/png"; // 默认类型
                }
                PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                PictureVO uploadPicture = pictureService.uploadPicture(
                        file, pictureUploadRequest, loginUser);
                imageResponse.setUploadPicture(uploadPicture);
                // 使用 Content.fromParts 发送图片和文字
                response = chat.sendMessage(
                        Content.fromParts(
                                Part.fromText(prompt),
                                Part.fromBytes(imageBytes, mimeType)));
            }

            // 处理 Gemini 返回的响应
            for (Part part : Objects.requireNonNull(response.parts())) {
                if (part.text().isPresent()) {
                    System.out.println(part.text().get());
                    imageResponse.setText(part.text().get());
                } else if (part.inlineData().isPresent()) {
                    var blob = part.inlineData().get();
                    if (blob.data().isPresent()) {
                        // 2. 将 Gemini 返回的字节数组转为 MultipartFile 上传到 COS
                        byte[] generatedImageBytes = blob.data().get();
                        String generatedMimeType = blob.mimeType().orElse("image/png");
                        String fileName = "ai_generated_" + UUID.randomUUID()
                                + getExtensionFromMimeType(generatedMimeType);

                        // 创建 MultipartFile 并上传到 COS
                        MultipartFile generatedFile = new Base64DecodedMultipartFile(
                                generatedImageBytes, fileName, generatedMimeType);

                        PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                        // 上传到 COS 并保存到数据库（待审核状态，备注为AI生成）
                        PictureVO generatePicture = pictureService.uploadPicture(
                                generatedFile, pictureUploadRequest, loginUser);
                        imageResponse.setGeneratePicture(generatePicture);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("图片生成失败: " + e.getMessage(), e);
        }

        return imageResponse;
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
