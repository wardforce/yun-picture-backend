package com.wuzhenhua.yunpicturebackend.api.gemini.service;

import com.wuzhenhua.yunpicturebackend.api.gemini.model.AiGenerateResponse;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.CreateChatRequest;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.CreateImageRequest;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.ImageResponse;

import jakarta.servlet.http.HttpServletRequest;


public interface AiPictureGeneratorService {


    /**
     * 原有方法（保持向后兼容）
     */
    @Deprecated
    ImageResponse generateImages(CreateImageRequest request, HttpServletRequest httpServletRequest);

    /**
     * Generates an AI-created image based on the provided request.
     *
     * @param request the request containing the prompt and optional image details
     * @param httpServletRequest the HTTP servlet request
     * @return an AiGenerateResponse containing the AI-generated image and associated chat history
     */
    AiGenerateResponse generateAiImage(CreateChatRequest request, HttpServletRequest httpServletRequest);

}
