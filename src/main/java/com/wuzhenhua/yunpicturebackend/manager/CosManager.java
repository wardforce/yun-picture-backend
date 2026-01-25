package com.wuzhenhua.yunpicturebackend.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.io.FileUtil;
import org.springframework.stereotype.Component;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.wuzhenhua.yunpicturebackend.config.CosClientConfig;

import jakarta.annotation.Resource;

@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传并解析图片的方法
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        //获取图片的基本信息
        PicOperations picOperations = new PicOperations();
        //返回原始信息
        picOperations.setIsPicInfo(1);
        //图片处理规则列表
        List<PicOperations.Rule> rules = new ArrayList<>();
        //1.图片压缩
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule =new PicOperations.Rule();
        compressRule.setFileId(webpKey);
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setRule("imageMogr2/format/webp");
        rules.add(compressRule);
        //2.缩略图处理,仅仅只对大于20kb的图片进行处理
        if (file.length() > 20 * 1024) {
            PicOperations.Rule thubnailRule =new PicOperations.Rule();
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail.webp"+FileUtil.getSuffix(key);
            thubnailRule.setFileId(thumbnailKey);
            thubnailRule.setBucket(cosClientConfig.getBucket());
            thubnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s", 256,256));
            rules.add(thubnailRule);
        }
        //构造处理函数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传并解析图片的方法
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putUrlPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        //获取图片的基本信息
        PicOperations picOperations = new PicOperations();
        //返回原始信息
        picOperations.setIsPicInfo(1);
        //图片处理规则列表
        List<PicOperations.Rule> rules = new ArrayList<>();
        //1.图片压缩
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setFileId(webpKey);
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setRule("imageMogr2/format/webp");
        rules.add(compressRule);

        //构造处理函数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }
    public void deleteObject(String key) {
        cosClient.deleteObject(cosClientConfig.getBucket(),key);
    }
}
