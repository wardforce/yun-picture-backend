package com.wuzhenhua.yunpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
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
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.wuzhenhua.yunpicturebackend.config.CosClientConfig;
import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.manager.CosManager;
import com.wuzhenhua.yunpicturebackend.service.AvatarUploadService;
import com.wuzhenhua.yunpicturebackend.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class AvatarUploadServiceImpl implements AvatarUploadService {

    @Resource
    private CosManager cosManager;
    @Resource
    private COSClient cosClient;

    @Resource
    private CosClientConfig cosClientConfig;

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;
    private static final List<String> ALLOWED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "webp");

    @Override
    public String uploadAvatarFromFile(MultipartFile file, Long userId) {
        validPicture(file);
        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.getSuffix(originalFilename);
        String uuid = RandomUtil.randomString(16);
        String uploadPath = String.format("avatar/%d_%d.%s", userId, System.currentTimeMillis(), extension);
        String filename = String.format("%d_%d.%s", userId, System.currentTimeMillis(), extension);
        File tempFile=null;
        try {
            tempFile =File.createTempFile(uploadPath, null);

            log.info("Created temp file: {}", tempFile.getAbsolutePath());
            file.transferTo(tempFile);
            log.info("Transferred file, size: {} bytes", tempFile.length());
            log.info("Uploading to COS with key: {}", filename);
            PutObjectResult putObjectResult = cosManager.putUrlPictureObject(uploadPath, tempFile);
            //获取图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //获取图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            String key = null;
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
                key = thumbnailCiobject.getKey();
            }
            return cosClientConfig.getHost() + "/" + key;
        } catch (Exception e) {
            log.error("上传头像失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }finally {
            if (tempFile != null && tempFile.exists()) {
                FileUtil.del(tempFile);
                log.info("Deleted temp file: {}", tempFile.getAbsolutePath());
            }
        }
    }

    @Override
    public String uploadAvatarFromUrl(String fileUrl, Long userId) {
        validUrlPicture(fileUrl);

        File tempFile = null;
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            String extension = FileUtil.getSuffix(path);

            // 如果URL没有扩展名，默认jpg
            if (StrUtil.isBlank(extension)) {
                extension = "jpg";
            }

            String uploadPath = String.format("avatar/%d_%d.%s", userId, System.currentTimeMillis(), extension);
            tempFile = File.createTempFile("avatar_url_", "." + extension);

            try (InputStream in = url.openStream()) {
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("从URL下载文件完成, size: {} bytes", tempFile.length());

            // 使用带图片处理的上传方法
            PutObjectResult putObjectResult = cosManager.putUrlPictureObject(uploadPath, tempFile);

            // 获取图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();

            String key;
            if (CollUtil.isNotEmpty(objectList)) {
                // 压缩成功，使用webp
                CIObject ciObject = objectList.get(0);
                key = ciObject.getKey();
                try {
                    cosClient.deleteObject(cosClientConfig.getBucket(), uploadPath);
                    log.info("原图已删除：{}", uploadPath);
                } catch (Exception e) {
                    log.warn("原图删除失败：{}, error={}", uploadPath, e.getMessage());
                }
            } else {
                // 压缩失败，保留原图
                key = uploadPath;
                log.warn("图片压缩失败，保留原图：{}", uploadPath);
            }

            return cosClientConfig.getHost() + "/" + key;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("从URL上传头像失败: {}", fileUrl, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 确保临时文件清理
            if (tempFile != null && tempFile.exists()) {
                FileUtil.del(tempFile);
                log.info("临时文件已删除: {}", tempFile.getAbsolutePath());
            }
        }
    }

    @Override
    public void deleteOldAvatar(String avatarUrl) {
        if (StrUtil.isBlank(avatarUrl)) {
            return;
        }

        try {
            String host = cosClientConfig.getHost();
            if (avatarUrl.startsWith(host)) {
                String key = avatarUrl.substring(host.length() + 1);
                cosManager.deleteObject(key);
                log.info("删除旧头像成功: {}", key);
            }
        } catch (Exception e) {
            log.error("删除旧头像失败，但不阻塞主流程: {}", avatarUrl, e);
        }
    }
    @Override
    public void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        //校验文件大小
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "图片文件大小不能超过" + (MAX_FILE_SIZE / (1024 * 1024*2)) + "MB");
        //校验文件格式
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //允许文件上传的集合或列表
        ThrowUtils.throwIf(!ALLOWED_FORMATS.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "图片文件格式必须为jpg、jpeg、png或webp");
    }
    @Override
    public void validUrlPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
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
                    ThrowUtils.throwIf(fileSize > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "图片文件大小不能超过" + (MAX_FILE_SIZE / (1024 * 1024*2)) + "MB");
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
