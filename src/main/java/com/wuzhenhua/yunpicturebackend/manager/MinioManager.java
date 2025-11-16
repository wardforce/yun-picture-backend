package com.wuzhenhua.yunpicturebackend.manager;

import com.wuzhenhua.yunpicturebackend.config.MinioConfig;
import com.wuzhenhua.yunpicturebackend.service.MinioService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class MinioManager {
    @Resource
    private MinioConfig minioConfig;
    @Resource
    private MinioClient minioClient;

}
