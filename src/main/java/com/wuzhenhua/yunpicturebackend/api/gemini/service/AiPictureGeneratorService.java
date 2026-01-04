package com.wuzhenhua.yunpicturebackend.api.gemini.service;

import com.wuzhenhua.yunpicturebackend.api.gemini.model.CreateImageRequest;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.ImageResponse;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.output.Response;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AiPictureGeneratorService {

    /**
     * Generates AI images based on the provided request.
     *
     * @param request The request containing image generation parameters.
     * @param httpServletRequest The HTTP servlet request.
     * @return The generated image response.
     */
    ImageResponse generateImages(CreateImageRequest request, HttpServletRequest httpServletRequest);
}
