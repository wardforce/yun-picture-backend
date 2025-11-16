package com.wuzhenhua.yunpicturebackend.service;

import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public interface MinioService {

    /**
     * 上传文件到指定桶并返回 MinIO 的写入响应（包含 etag、bucket、object 等信息）。
     *
     * @param bucket       桶名称
     * @param objectName   对象名
     * @param inputStream  文件输入流
     * @return ObjectWriteResponse MinIO 写入结果
     */
    public ObjectWriteResponse putFile(String bucket,String objectName, InputStream inputStream) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    public InputStream downloadFile( String objectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
}
