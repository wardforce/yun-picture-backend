package com.wuzhenhua.yunpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * url文件上传
 * @author wardforce
 */
@Slf4j
@Service
public class UrlPictureUpload extends PictureUploadTemplate{
    @Override
    protected void validPicture(Object inputSource) {
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

    @Override
    protected String getOriginFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.mainName( fileUrl);
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        //下载到临时目录
        HttpUtil.downloadFile(fileUrl, file);
    }
}
