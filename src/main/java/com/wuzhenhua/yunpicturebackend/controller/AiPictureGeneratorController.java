package com.wuzhenhua.yunpicturebackend.controller;

import com.wuzhenhua.yunpicturebackend.api.gemini.model.CreateImageRequest;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.ImageResponse;
import com.wuzhenhua.yunpicturebackend.api.gemini.service.AiPictureGeneratorService;
import com.wuzhenhua.yunpicturebackend.common.BaseResponse;
import com.wuzhenhua.yunpicturebackend.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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
     * 生成图片
     *
     * @param file
     * @param httpServletRequest
     * @return
     */
    @PostMapping(path = "/generate_picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "生成图片", description = "根据文字或文字+图片生成新图片")
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
