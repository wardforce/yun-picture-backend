package com.wuzhenhua.yunpicturebackend.controller;

import java.io.File;
import java.io.IOException;

import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.qcloud.cos.model.COSObject;
import com.wuzhenhua.yunpicturebackend.annotation.AuthCheck;
import com.wuzhenhua.yunpicturebackend.common.BaseResponse;
import com.wuzhenhua.yunpicturebackend.constant.UserConstant;
import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.manager.CosManager;
import com.wuzhenhua.yunpicturebackend.utils.ResultUtils;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 测试文件上传
     *
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
   @Operation(summary = "测试上传文件")   
    @PostMapping(value = "/test/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<String> testUploadFile(@RequestParam("file") MultipartFile multipartFile) {
        // 文件目录  
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);
        File file = null;
        try {
            // 上传文件  
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址  
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件  
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }
    /**
     * 测试文件下载
     *
     * @param filepath
     * @param response
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
   @Operation(summary = "测试下载文件")   
    @GetMapping(value = "/test/download")
    public void testDownloadFile(String filepath,HttpServletResponse response) {
        // 下载文件  
      COSObjectInputStream cosObjectInputStream = null;
        byte[] bytes = null;
        try {
            // 设置响应头
             COSObject cosObject = cosManager.getObject(filepath);
         cosObjectInputStream = cosObject.getObjectContent();
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            bytes = IOUtils.toByteArray(cosObjectInputStream);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        }finally{
            if (cosObjectInputStream != null) {
                try {
                    cosObjectInputStream.close();
                } catch (IOException e) {
                    log.error("cosObjectInputStream close error", e);
                }
            }
        }


    }

}
