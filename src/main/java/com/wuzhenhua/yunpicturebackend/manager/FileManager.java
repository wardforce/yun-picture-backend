/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wuzhenhua.yunpicturebackend.manager;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.OriginalInfo;
import com.wuzhenhua.yunpicturebackend.config.CosClientConfig;
import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.model.dto.file.UploadPictureResult;
import com.wuzhenhua.yunpicturebackend.utils.ThrowUtils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author ward
 */
@Slf4j
@Service
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;
    @Resource
    private CosManager cosManager;

    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String UploadPathPrefix) {
        //校验图片
        validateImage(multipartFile);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        //自己拼接文件上传名称，不适用原始名称，
        String objectName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("%s/%s", UploadPathPrefix, objectName);
        //解析结果并返回
        File file = null;
        try {
            // 上传文件  
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObject = cosManager.putPictureObject(uploadPath, file);
            //获取图片信息
           OriginalInfo originalInfo = putObject.getCiUploadResult().getOriginalInfo();
           ImageInfo imageInfo = putObject.getCiUploadResult().getOriginalInfo().getImageInfo();
           //返回图片信息
           UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost()+"/"+uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(imageInfo.getWidth());
            uploadPictureResult.setPicHeight(imageInfo.getHeight());
            uploadPictureResult.setPicScale((double) NumberUtil.round((double) imageInfo.getWidth() / imageInfo.getHeight(), 2).doubleValue());
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
           return uploadPictureResult;
        } catch (Exception e) {
            log.error("file upload error, uploadPath = " + uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            deleteTempFile(file);
        }
    }

    /**
     *清理临时文件
     * @param file
     */
    private static void deleteTempFile(File file) {
        if (file != null) {
            // 删除临时文件  
            boolean deleteResult = file.delete();
            if (!deleteResult) {
                log.error("file delete error, filepath = {}", file.getAbsolutePath());
            }
        }
    }

    /**
     * 校验图片
     *
     * @param multipartFile 图片文件
     */
    private void validateImage(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        //校验文件大小
        long fileSize = multipartFile.getSize();
        final long MAX_FILE_SIZE = 1024 * 1024 * 10;
        ThrowUtils.throwIf(fileSize > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "图片文件大小不能超过" + (MAX_FILE_SIZE / (1024 * 1024*10)) + "MB");
        //校验文件格式
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //允许文件上传的集合或列表
        final List<String> ALLOWED_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");
        ThrowUtils.throwIf(!ALLOWED_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "图片文件格式必须为jpg、jpeg、png或webp");
    }
}
