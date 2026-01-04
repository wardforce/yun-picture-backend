package com.wuzhenhua.yunpicturebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuzhenhua.yunpicturebackend.api.aliyun.model.CreateOutPaintingTaskResponse;
import com.wuzhenhua.yunpicturebackend.model.dto.picture.*;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.entity.Picture;
import com.wuzhenhua.yunpicturebackend.model.vo.PictureVO;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author ward
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-11-16 23:07:18
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inpurSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inpurSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取查询包装器
     *
     * @param pictureQueryRequest
     * @return
     */
    public LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);
    /**
     * 获取图片封装类
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);
    /**
     * 分页获取图片封装
     * @param picturePage
     * @param request
     * @return
     */
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片
     *
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest,User loginUser);

    /**
     * 填充审核参数
     * @param picture
     * @param loginUser
     */
    public void fillReviewParams(Picture picture,User loginUser);

    /**
     * 批量抓取图片
     * @param pictureUploadRequest
     * @param loginUser
     * @return 成功的图片数量
     */
    Integer uploadPictureByBatche(PictureUploadByBatchRequest pictureUploadRequest, User loginUser);
    /**
     * 清除图片文件
     * @param picture
     */
    void clearPictureFile(Picture picture);

    /**
     * 校验空间图片的权限
     *
     * @param picture   the Picture object representing the image for which authorization needs to be checked
     * @param loginUser the User object representing the currently logged-in user
     */
    void checkPictureAuth(Picture picture, User loginUser);

    /**
     * 删除图片
     *
     * @param pictureId
     * @param loginUser
     */
    void deletePicture(long pictureId, User loginUser);

    /**
     * 编辑图片(用户接口)
     *
     * @param pictureEditRequest
     * @param loginUser
     */
    void editorPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 根据颜色搜索图片
     * @param spaceId
     * @param picColor
     * @param loginuser
     * @return
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginuser);

    /**
     * 批量编辑图片
     *
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    /**
     * 根据请求参数和用户信息，为指定图片创建新的扩图任务。
     * 该操作通常用于通过内容生成来扩展图片的边界。
     *
     * @param createPictureOutPaintingTaskRequest 包含图片信息和扩图参数的请求对象
     * @param loginUser                           发起任务的用户，用于授权或上下文判断
     * @return 包含已创建扩图任务的任务 ID 与状态的响应对象
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
}
