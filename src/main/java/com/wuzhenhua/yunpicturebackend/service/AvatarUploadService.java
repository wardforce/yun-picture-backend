package com.wuzhenhua.yunpicturebackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface AvatarUploadService {

    /**
     * 从文件上传头像
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return COS URL
     */
    String uploadAvatarFromFile(MultipartFile file, Long userId);

    /**
     * 从URL上传头像
     *
     * @param fileUrl 文件URL
     * @param userId 用户ID
     * @return COS URL
     */
    String uploadAvatarFromUrl(String fileUrl, Long userId);

    /**
     * 删除旧头像
     *
     * @param avatarUrl 头像URL
     */
    void deleteOldAvatar(String avatarUrl);
    /**
     * 校验图片文件
     *
     * @param inputSource 输入源
     */
    void validPicture(Object inputSource);


    void validUrlPicture(Object inputSource);
}
