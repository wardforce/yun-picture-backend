package com.wuzhenhua.yunpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.manager.FileManager;
import com.wuzhenhua.yunpicturebackend.mapper.pictureMapper;
import com.wuzhenhua.yunpicturebackend.model.dto.file.UploadPictureResult;
import com.wuzhenhua.yunpicturebackend.model.dto.picture.PictureQueryRequest;
import com.wuzhenhua.yunpicturebackend.model.dto.picture.PictureReviewRequest;
import com.wuzhenhua.yunpicturebackend.model.dto.picture.PictureUploadRequest;
import com.wuzhenhua.yunpicturebackend.model.entity.Picture;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.enums.PictureReviewStatusEnum;
import com.wuzhenhua.yunpicturebackend.model.vo.PictureVO;
import com.wuzhenhua.yunpicturebackend.model.vo.UserVO;
import com.wuzhenhua.yunpicturebackend.service.PictureService;
import com.wuzhenhua.yunpicturebackend.service.UserService;
import com.wuzhenhua.yunpicturebackend.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ward
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-11-16 23:07:18
 */
@Service
@Slf4j
public class pictureServiceImpl extends ServiceImpl<pictureMapper, Picture>
        implements PictureService {

    @Resource
    private UserService userService;
    @Resource
    private FileManager fileManager;


    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验图片
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户不能为空");
        //判断是新增还是删除
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            //新增 
            pictureId = pictureUploadRequest.getId();
        }
        //如果是更新，还要判断图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            //仅仅只有本人或者管理员可以编辑图片
            ThrowUtils.throwIf(!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "没有权限");
        }
        //上传图片,得到图片信息
        //安装用户id划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPicture = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        //构造入库图片信息
        Picture picture = new Picture();
        log.info("uploadPicture result for userId={}, picName={}, size={}", loginUser.getId(), uploadPicture.getPicName(), uploadPicture.getPicSize());
        picture.setUrl(uploadPicture.getUrl());
        picture.setName(uploadPicture.getPicName());
        picture.setPicSize(uploadPicture.getPicSize());
        picture.setPicWidth(uploadPicture.getPicWidth());
        picture.setPicHeight(uploadPicture.getPicHeight());
        picture.setPicScale(uploadPicture.getPicScale());
        picture.setPicFormat(uploadPicture.getPicFormat());
        picture.setUserId(loginUser.getId());
        //补充审核参数
        this.fillReviewParams(picture, loginUser);
        //操作数据库
        //如果pictureId为空，说明是新增
        if (pictureId != null) {
            //更新，要补充id和和编辑时间
            picture.setId(pictureId);
            picture.setUpdateTime(new Date());
        }
        boolean saveOrUpdate = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!saveOrUpdate, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureVO.objToVo(picture);
    }

    /**
     * 获取查询包装器
     *
     * @param pictureQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        LambdaQueryWrapper<Picture> queryWrapper = new LambdaQueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值  
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like(Picture::getName, searchText)
                    .or()
                    .like(Picture::getIntroduction, searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), Picture::getId, id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), Picture::getUserId, userId);
        queryWrapper.like(StrUtil.isNotBlank(name), Picture::getName, name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), Picture::getIntroduction, introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), Picture::getPicFormat, picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), Picture::getCategory, category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), Picture::getPicWidth, picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), Picture::getPicHeight, picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), Picture::getPicSize, picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), Picture::getPicScale, picScale);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like(Picture::getTags, "\"" + tag + "\"");
            }
        }
        // 安全的排序处理：仅允许白名单字段，避免 LambdaQueryWrapper 传入字符串导致的错误以及 SQL 注入风险
        if (StrUtil.isNotBlank(sortField)) {
            // 兼容前端常见的排序关键字：asc/desc 和 ascend/descend
            boolean asc = "ascend".equalsIgnoreCase(sortOrder) || "asc".equalsIgnoreCase(sortOrder);

            // 将可排序的前端字段映射到实体的 Getter（SFunction）
            java.util.Map<String, SFunction<Picture, ?>> sortMap = new java.util.HashMap<>();
            sortMap.put("id", Picture::getId);
            sortMap.put("name", Picture::getName);
            sortMap.put("introduction", Picture::getIntroduction);
            sortMap.put("category", Picture::getCategory);
            sortMap.put("picSize", Picture::getPicSize);
            sortMap.put("picWidth", Picture::getPicWidth);
            sortMap.put("picHeight", Picture::getPicHeight);
            sortMap.put("picScale", Picture::getPicScale);
            sortMap.put("picFormat", Picture::getPicFormat);
            sortMap.put("userId", Picture::getUserId);
            sortMap.put("createTime", Picture::getCreateTime);
            sortMap.put("editTime", Picture::getEditTime);  // 修复的关键字段
            sortMap.put("updateTime", Picture::getUpdateTime);

            // 获取对应的列，如果存在则应用排序
            SFunction<Picture, ?> column = sortMap.get(sortField);
            if (column != null) {
                if (asc) {
                    queryWrapper.orderByAsc(column);
                } else {
                    queryWrapper.orderByDesc(column);
                }
            }
        }
        return queryWrapper;
    }

    /**
     * 获取图片封装类
     *
     * @param picture
     * @param request
     * @return
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类  
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息  
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    /**
     * 校验图片信息
     *
     * @param picture 图片信息
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //1.校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR, "图片审核参数不能为空");
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getPictureReviewStatusEnumByValue(reviewStatus);
        ThrowUtils.throwIf(id == null || reviewStatus == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum), ErrorCode.PARAMS_ERROR, "图片id和审核状态不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR, "登录用户不能为空");
        //2.判断图片是否存在
        com.wuzhenhua.yunpicturebackend.model.entity.Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //3.校验审核状态是否重复，已经审核了
        ThrowUtils.throwIf(oldPicture.getReviewStatus().equals(reviewStatusEnum), ErrorCode.PARAMS_ERROR, "请勿重复审核");
        //4.数据库操作
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片审核失败");
    }

    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            //管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
            picture.setReviewMessage("管理员自动过审");
        }//非管理员，无论是编辑还是创建都需要审核
        else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }
}
