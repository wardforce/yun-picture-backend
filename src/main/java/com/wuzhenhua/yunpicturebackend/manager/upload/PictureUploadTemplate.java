/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wuzhenhua.yunpicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.OriginalInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.qcloud.cos.transfer.TransferManager;
import com.wuzhenhua.yunpicturebackend.config.CosClientConfig;
import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.manager.CosManager;
import com.wuzhenhua.yunpicturebackend.model.dto.file.UploadPictureResult;
import com.wuzhenhua.yunpicturebackend.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 图片上传模板
 *
 * @author ward
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;
    @Resource
    private CosManager cosManager;

    /**
     * 校验输入源（本地文件或 URL）
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取输入源的原始文件名
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * 处理输入源并生成本地临时文件
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;

    /**
     * 上传图片模板
     *
     * @param inputSource      the image file to be uploaded
     * @param UploadPathPrefix the prefix for the upload path where the image will be stored
     * @return an {@code UploadPictureResult} object containing details of the uploaded picture such as URL, name, size, dimensions, scale, and format
     * @throws BusinessException if the image is invalid, upload fails, or any system error occurs
     */
    public UploadPictureResult uploadPicture(Object inputSource, String UploadPathPrefix) {
        //校验图片
        validPicture(inputSource);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginFilename(inputSource);
        //自己拼接文件上传名称，不适用原始名称，
        String objectName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("%s/%s", UploadPathPrefix, objectName);
        //解析结果并返回
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null);
            //处理图片来源
            processFile(inputSource, file);
            PutObjectResult putObject = cosManager.putPictureObject(uploadPath, file);
            //获取图片信息
            ImageInfo imageInfo = putObject.getCiUploadResult().getOriginalInfo().getImageInfo();
            //获取图片处理结果
            ProcessResults processResults = putObject.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                //获取压缩后的文件信息
                CIObject ciObject = objectList.get(0);
                CIObject thumbnailCiobject = null;
                if (objectList.size() > 1) {
                    thumbnailCiobject = objectList.get(1);
                } else
                    //缩略图默认为压缩图
                    thumbnailCiobject = ciObject;
                // 压缩成功后，删除原图以节省存储空间（数据库URL已指向压缩图）
                try {
                    cosClient.deleteObject(cosClientConfig.getBucket(), uploadPath);
                    log.info("原图已删除：{}", uploadPath);
                } catch (Exception e) {
                    // 删除失败不影响业务，仅记录日志
                    log.warn("原图删除失败：{}, error={}", uploadPath, e.getMessage());
                }
                return buildResult(ciObject, thumbnailCiobject, originalFilename, imageInfo);
            }
            return buildResult(uploadPath, originalFilename, file, imageInfo);
        } catch (Exception e) {
            log.error("file upload error, uploadPath = " + uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            deleteTempFile(file);
        }
    }

    /**
     * 封装返回结果
     *
     * @param ciObject          压缩后的对象
     * @param thumbnailCiobject 缩略图对象
     * @param originalFilename  原始文件名字
     * @return
     * @
     */
    private UploadPictureResult buildResult(CIObject ciObject, CIObject thumbnailCiobject, String originalFilename,ImageInfo imageInfo) {
        //返回压缩后图片信息
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + ciObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(ciObject.getSize().longValue());
        uploadPictureResult.setPicWidth(ciObject.getWidth());
        uploadPictureResult.setPicHeight(ciObject.getHeight());
        uploadPictureResult.setPicScale((double) NumberUtil.round((double) ciObject.getWidth() / ciObject.getHeight(), 2).doubleValue());
        uploadPictureResult.setPicFormat(ciObject.getFormat());
        //获取图片主色调
        uploadPictureResult.setPicColor(imageInfo.getAve());
        //设置缩略图地址
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiobject.getKey());
        return uploadPictureResult;
    }

    /**
     * 封装返回结果
     *
     * @param uploadPath       路径
     * @param originalFilename 原始文件名字
     * @param file             原始文件
     * @param imageInfo        对象存储返回的图片信息
     * @return
     */
    private UploadPictureResult buildResult(String uploadPath, String originalFilename, File file, ImageInfo imageInfo) {
        //返回图片信息
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(imageInfo.getWidth());
        uploadPictureResult.setPicHeight(imageInfo.getHeight());
        uploadPictureResult.setPicScale((double) NumberUtil.round((double) imageInfo.getWidth() / imageInfo.getHeight(), 2).doubleValue());
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        //获取图片主色调
        uploadPictureResult.setPicColor(imageInfo.getAve());
        return uploadPictureResult;
    }


    /**
     * 清理临时文件
     *
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


}
