package com.wuzhenhua.yunpicturebackend.controller;

import com.wuzhenhua.yunpicturebackend.annotation.AuthCheck;
import com.wuzhenhua.yunpicturebackend.common.BaseResponse;
import com.wuzhenhua.yunpicturebackend.config.MinioConfig;
import com.wuzhenhua.yunpicturebackend.constant.UserConstant;
import com.wuzhenhua.yunpicturebackend.service.MinioService;
import com.wuzhenhua.yunpicturebackend.utill.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/file")
@Tag(name = "FileController", description = "文件上传检查")
public class FileController {
    @Resource
    private  MinioService minioService;
    @Resource
    private MinioConfig minioConfig;
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping(value = "/test/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "文件上传检查", description = "检查文件上传是否运行正常")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40101", description = "无权限"),
            @ApiResponse(responseCode = "50000", description = "系统内部异常"),
    })
    public BaseResponse<String> testUploadFile(@Parameter(description = "上传的文件", required = true)
                                                    @RequestPart("file") MultipartFile file) {
        String originalFilename = file.getOriginalFilename();  // 这里可以拿到上传文件名
        // 你可以做一些规范化处理，比如防止 null、去路径等
        String objectName = System.currentTimeMillis() + "_" + originalFilename;

        try {
           minioService.putFile(minioConfig.getBucketName(), objectName, file.getInputStream());
        } catch (Exception e) {
            log.error("MinIO 服务异常： {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return ResultUtils.success(objectName);
    }
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download")
    @Operation(summary = "文件下载检查", description = "检查文件下载是否运行正常")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "下载成功"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40101", description = "无权限"),
            @ApiResponse(responseCode = "50000", description = "系统内部异常"),
    })
    public void testDownloadFile(@Parameter(description = "文件对象名称", required = true)
                                 @RequestParam("objectName") String objectName,
                                 HttpServletResponse response) {
        // 使用 try-with-resources 自动关闭流资源
        try (InputStream inputStream = minioService.downloadFile(objectName);
             OutputStream outputStream = response.getOutputStream()) {
            
            // 设置响应头
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(objectName, StandardCharsets.UTF_8));
            
            // 将文件流写入响应输出流
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
        // 注意: try-with-resources 会自动关闭 InputStream 和 OutputStream,
        // 无需手动在 finally 块中关闭,即使发生异常也会保证关闭
    }
}
