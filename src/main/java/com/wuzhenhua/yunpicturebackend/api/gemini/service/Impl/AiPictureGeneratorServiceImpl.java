package com.wuzhenhua.yunpicturebackend.api.gemini.service.Impl;

import autovalue.shaded.com.google.common.collect.ImmutableList;
import cn.hutool.core.util.ObjUtil;
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

import com.wuzhenhua.yunpicturebackend.model.enums.UserRoleEnum;
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
import java.util.*;

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
    @Deprecated
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

                // 2. 优先使用 thumbnailUrl，如果没有则降级使用 url
                String imageUrl = uploadPicture.getThumbnailUrl();
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = uploadPicture.getUrl();
                    log.warn("图片 {} 没有缩略图，使用原图 URL", uploadPicture.getId());
                }
                byte[] compressedImageBytes = downloadImageFromUrl(imageUrl);

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
        List<Long> pictureIds = request.getPictureIds();
        Long requestSpaceId = request.getSpaceId();

        Long aigeneratedPictureSpaceId = null;
        if (ObjUtil.isNotNull(requestSpaceId)) {
            aigeneratedPictureSpaceId = requestSpaceId;
        }

        // 2. 校验图片数量（最多14张）
        if (pictureIds != null && pictureIds.size() > 14) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多支持14张参考图片");
        }
        ChatHistory userChatHistory = null;

        if (requestSpaceId == null || ObjUtil.isNull(requestSpaceId)) {
            // 3. 保存用户消息到 chat_history（支持多图片）
            userChatHistory = chatHistoryService.saveUserMessage(
                    loginUser.getId(), prompt, pictureIds, request.getSessionId());
            log.info("generateAiImage - 用户消息已保存, chatHistoryId: {}", userChatHistory.getId());
        } else {
            // 3. 保存用户消息到 chat_history（支持多图片）
            userChatHistory = chatHistoryService.saveUserMessage(
                    loginUser.getId(), prompt, pictureIds, request.getSessionId(), requestSpaceId);
            log.info("generateAiImage - 用户消息已保存, chatHistoryId: {}", userChatHistory.getId());
        }

        AiGenerateResponse response = new AiGenerateResponse();

        // 配置 Gemini 生成内容
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities(ImmutableList.of("IMAGE", "TEXT"))
                .systemInstruction(
                        Content.fromParts(Part.fromText("""
                                # 角色与目标
                                
                                你是一位高级 AI 艺术总监和图像生成专家。你的目标是基于用户的【文字描述】和配置的【参考图片】，合成一张画面统一、高质量的图像。
                                
                                # 输入数据
                                
                                - **用户描述**: 
                                - **参考图片**: 附件中的图片（最多14张）是关键的视觉风格指南。
                                
                                # 执行指令
                                
                                1. **深度分析参考图**: 仔细分析附件中的参考图片，提取以下要素：
                                    - *视觉风格*: 艺术媒介（如油画、3D渲染、摄影）、线条处理和阴影风格。
                                    - *色彩板*: 主色调、光照色温和饱和度。
                                    - *氛围*: 整体情绪（如空灵、赛博朋克、极简主义）。
                                2. **合成生成**: 将【用户描述】中定义的具体**主体内容**，与从【参考图片】中提取的**样式和氛围**相结合。
                                    - *优先级规则*: 【用户描述】决定“画什么”（内容），【参考图片】决定“怎么画”（形式/风格）。
                                3. **技术规范**:
                                    - 确保高保真度，人体解剖比例正确，光照逻辑一致。
                                    - 如果参考图片风格相互冲突，请优先参考前 3 张图片的风格。
                                    - 输出分辨率：高（确保细节密度符合专业标准）。
                                
                                # 生成命令
                                
                                立即生成图像，严格遵循参考图片的视觉语言，并应用于以下描述的场景：
                                【用户描述】: %s
                                
                                --
                                *保持自然的光照、符合物理规律的阴影，以及参考图片中确切的纹理质感。*
                                """)))
                .build();

        try {
            List<Content> contents;

            if (pictureIds == null || pictureIds.isEmpty()) {
                // 仅文字生成图片
                log.info("generateAiImage - 发送纯文本请求到 Gemini API");
                contents = ImmutableList.of(Content.builder()
                        .role("user")
                        .parts(ImmutableList.of(Part.fromText(prompt)))
                        .build());
            } else {
                // 多图片+文字生成新图片
                List<Part> partsList = new ArrayList<>();
                partsList.add(Part.fromText(prompt));

                // 下载并添加所有图片
                for (Long pictureId : pictureIds) {
                    // 空值检查：确保图片存在
                    var pictureEntity = pictureService.getById(pictureId);
                    if (pictureEntity == null) {
                        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,
                                "图片不存在, pictureId: " + pictureId);
                    }

                    Long pictureSpaceId = pictureEntity.getSpaceId();

                    // 如果图片属于某个空间，进行权限检查
                    if (ObjUtil.isNotNull(pictureSpaceId)) {
                        // 非管理员用户只能使用自己的空间图片
                        if (!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
                            ThrowUtils.throwIf(!loginUser.getId().equals(pictureEntity.getUserId()),
                                    ErrorCode.NO_AUTH_ERROR, "无权使用他人空间的图片");
                        }
                        // 如果任一图片有 spaceId，生成的图片也放到该空间
                        if (aigeneratedPictureSpaceId == null) {
                            aigeneratedPictureSpaceId = pictureSpaceId;
                        }
                    }

                    PictureVO picture = pictureService.getPictureVO(pictureEntity, httpServletRequest);
                    if (picture == null) {
                        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,
                                "无法获取图片信息, pictureId: " + pictureId);
                    }
                    // 优先使用 thumbnailUrl，如果没有则降级使用 url
                    String imageUrl = picture.getThumbnailUrl();
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        imageUrl = picture.getUrl();
                        log.warn("图片 {} 没有缩略图，使用原图 URL", pictureId);
                    }
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        throw new BusinessException(ErrorCode.OPERATION_ERROR,
                                "图片 URL 为空, pictureId: " + pictureId);
                    }
                    byte[] imageBytes = downloadImageFromUrl(imageUrl);
                    partsList.add(Part.fromBytes(imageBytes, "image/webp"));
                }

                log.info("generateAiImage - 发送{}张图片+文本请求到 Gemini API", pictureIds.size());

                contents = ImmutableList.of(Content.builder()
                        .role("user")
                        .parts(ImmutableList.copyOf(partsList))
                        .build());
            }

            // 调用 Gemini API
            GenerateContentResponse geminiResponse = gemini.client.models.generateContent(
                    gemini.modelName, contents, config);

            String aiText = null;
            List<PictureVO> generatedPictures = new ArrayList<>();

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
                            if (aigeneratedPictureSpaceId != null) {
                                pictureUploadRequest.setSpaceId(aigeneratedPictureSpaceId);
                            }
                            PictureVO generatedPicture = pictureService.uploadPicture(
                                    generatedFile, pictureUploadRequest, loginUser);
                            generatedPictures.add(generatedPicture);
                        }
                    } else if (part.text().isPresent()) {
                        aiText = part.text().get();
                        log.info("generateAiImage - 收到 Gemini 文本响应: {}", aiText);
                    }
                }
            }

            // 保存 AI 消息到 chat_history（支持多图片）
            List<Long> generatedPictureIds = generatedPictures.stream()
                    .map(PictureVO::getId)
                    .toList();
            Long userSessionId = userChatHistory.getSessionId();

            ChatHistory aiChatHistory = null;
            if (aigeneratedPictureSpaceId == null || ObjUtil.isNull(aigeneratedPictureSpaceId)) {
                aiChatHistory = chatHistoryService.saveAiMessage(
                        loginUser.getId(), aiText != null ? aiText : "", generatedPictureIds, userSessionId);
            } else
                aiChatHistory = chatHistoryService.saveAiMessage(
                        loginUser.getId(), aiText != null ? aiText : "", generatedPictureIds, userSessionId,
                        aigeneratedPictureSpaceId);

            // 设置响应
            response.setChatHistory(aiChatHistory);
            response.setPictureVOs(generatedPictures);
            response.setAiText(aiText);

            log.info("generateAiImage - AI 消息已保存, chatHistoryId: {}, 生成图片数: {}",
                    aiChatHistory.getId(), generatedPictures.size());
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
