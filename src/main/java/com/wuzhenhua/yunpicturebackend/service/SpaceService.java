package com.wuzhenhua.yunpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import com.wuzhenhua.yunpicturebackend.model.dto.space.SpaceAddRequest;
import com.wuzhenhua.yunpicturebackend.model.dto.space.SpaceQueryRequest;
import com.wuzhenhua.yunpicturebackend.model.entity.Space;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.vo.SpaceVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author ward
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-12-21 19:28:17
*/
public interface SpaceService extends IService<Space> {
    /**
     * 添加空间
     * @param spaceaddRequest
     * @param user
     * @return
     */
    long addSpace(SpaceAddRequest spaceaddRequest, User user);
    /**
     * 获取查询包装器
     *
     * @param spaceQueryRequest
     * @return
     */
    public LambdaQueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);
    /**
     * 获取空间封装类
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);
    /**
     * 分页获取空间封装
     * @param spacePage
     * @param request
     * @return
     */
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 校验空间
     * @param space
     */
    void validSpace(Space space,boolean add);
    /**
     * 根据空间等级填充空间信息
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);
}
