package com.wuzhenhua.yunpicturebackend.controller;

import com.wuzhenhua.yunpicturebackend.api.gemini.model.AiGenerateResponse;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.CreateChatRequest;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.CreateImageRequest;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.ImageResponse;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.PrepareImageResponse;
import com.wuzhenhua.yunpicturebackend.api.gemini.service.AiPictureGeneratorService;
import com.wuzhenhua.yunpicturebackend.common.BaseResponse;
import com.wuzhenhua.yunpicturebackend.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "AiPictureGeneratorController", description = "AI图片生成控制器")
@Slf4j
@RestController
@RequestMapping("/ai_picture_generator")
public class AiPictureGeneratorController {

    @Resource
    private AiPictureGeneratorService aiPictureGeneratorService;

    /**
     * AI 生成图片
     *
     */
    @PostMapping("/generate_ai_image")
    @Operation(summary = "AI生成图片", description = "第二阶段：调用AI生成图片")
    public BaseResponse<AiGenerateResponse> generateAiImage(
            @RequestBody CreateChatRequest createChatRequest,
            HttpServletRequest httpServletRequest) {
        AiGenerateResponse response = aiPictureGeneratorService.generateAiImage(createChatRequest, httpServletRequest);
        return ResultUtils.success(response);
    }

    /**
     * 原有方法（保持向后兼容）
     * 一次性完成上传和生成
     */
    @Deprecated
    @PostMapping(path = "/generate_picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "生成图片(测试)", description = "根据文字或文字+图片生成新图片（一次性完成）")
    public BaseResponse<ImageResponse> generatePicture(
            @RequestParam("prompt") String prompt,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpServletRequest httpServletRequest) {
        CreateImageRequest request = new CreateImageRequest();
        request.setPrompt(prompt);
        request.setFile(file);
        ImageResponse imageResponse = aiPictureGeneratorService.generateImages(request, httpServletRequest);
        return ResultUtils.success(imageResponse);
    }
}
