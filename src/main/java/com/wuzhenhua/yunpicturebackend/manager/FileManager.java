/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wuzhenhua.yunpicturebackend.manager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.*;
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
 * 文件服务，已经废弃，改为使用upload模板包的方法
 * @author ward
 */
@Slf4j
@Service
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;
    @Resource
    private CosManager cosManager;

    /**
     * Uploads an image to a specified location and returns the result containing image details.
     * This method verifies the image, uploads it to the cloud storage, and provides metadata about the uploaded image.
     *
     * @param multipartFile the image file to be uploaded
     * @param UploadPathPrefix the prefix for the upload path where the image will be stored
     * @return an {@code UploadPictureResult} object containing details of the uploaded picture such as URL, name, size, dimensions, scale, and format
     * @throws BusinessException if the image is invalid, upload fails, or any system error occurs
     */
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
     * 校验图片(上传文件)
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
    //todo 新增方法

    /**
     * 通过url上传图片by url
     * @param fileUrl   文件地址
     * @param UploadPathPrefix   上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPictureByUrl(String fileUrl, String UploadPathPrefix) {
        //校验图片url
        // todo
        validatePicture(fileUrl);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        // todo 获取文件名
        String originalFilename = FileUtil.mainName( fileUrl);
        //自己拼接文件上传名称，不适用原始名称，
        String objectName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("%s/%s", UploadPathPrefix, objectName);
        //解析结果并返回
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(uploadPath, null);
            // todo
            HttpUtil.downloadFile(fileUrl, file);
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
     * 校验图片(通过url)
     * @param fileUrl
     */
    private void validatePicture(String fileUrl) {
        //校验非空
        ThrowUtils.throwIf(fileUrl == null, ErrorCode.PARAMS_ERROR,"文件地址为空");
        //校验URL格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }
        //校验URL协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http") && !fileUrl.startsWith("https"), ErrorCode.PARAMS_ERROR, "仅支持http或https协议的文件地址");
        //发送HEAD请求验证文件是否存在
        HttpResponse httpResponse =null;
        try {
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            //没有正常返回不判断
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {return;}
            //文件存在、类型校验
            String contentType = httpResponse.header("Content-Type");
            //不为空，才需要校验是否合法
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            //文件存在、校验大小
            String contentLength = httpResponse.header("Content-Length");
            if (StrUtil.isNotBlank(contentLength)) {
                long fileSize = 0;
                try {
                    fileSize = Long.parseLong(contentLength);
                    final long MAX_FILE_SIZE = 1024 * 1024 * 10;
                    ThrowUtils.throwIf(fileSize > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "图片文件大小不能超过" + (MAX_FILE_SIZE / (1024 * 1024*10)) + "MB");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式异常");
                }
            }
        }
         finally {
            if (httpResponse != null) {httpResponse.close();}
        }
    }
}
