package com.wuzhenhua.yunpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.model.dto.space.SpaceAddRequest;
import com.wuzhenhua.yunpicturebackend.model.dto.space.SpaceQueryRequest;
import com.wuzhenhua.yunpicturebackend.model.entity.Space;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.enums.SpaceLevelEnum;
import com.wuzhenhua.yunpicturebackend.model.vo.SpaceVO;
import com.wuzhenhua.yunpicturebackend.model.vo.UserVO;
import com.wuzhenhua.yunpicturebackend.service.UserService;
import com.wuzhenhua.yunpicturebackend.service.SpaceService;
import com.wuzhenhua.yunpicturebackend.mapper.SpaceMapper;
import com.wuzhenhua.yunpicturebackend.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author ward
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-12-21 19:28:17
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService {
    @Resource
    private UserService userService;
    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 创建空间
     * @param spaceaddRequest
     * @param user
     * @return
     */
    @Override
    public long addSpace(SpaceAddRequest spaceaddRequest, User user) {
        //填充参数默认值
        Space space=new Space();
        BeanUtils.copyProperties(spaceaddRequest,space);
        if(StrUtil.isBlank(space.getSpaceName())){
            space.setSpaceName("默认空间");
        }
        if(space.getSpaceLevel()==null){
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        //填充容量和大小
        this.fillSpaceBySpaceLevel(space);
       //校验参数
        this.validSpace(space,true);
        space.setUserId(user.getId());
        if(SpaceLevelEnum.COMMON.getValue()!=space.getSpaceLevel()&&!userService.isAdmin(user)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"没有权限创建指定级别的空间");
        }
        //控制同一个用户只能创建一个私有空间
        String lock=String.valueOf(user.getId()).intern();
        synchronized (lock){
            Long newSpaceId = transactionTemplate.execute(status -> {
                //判断是否存在空间
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, user.getId())
                        .exists();
                //如果已经有了空间，就不能再次创建
                if (exists) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已经存在私有空间");
                }
                boolean save = this.save(space);
                ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "保存空间失败");
                return space.getId();
            });
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    @Override
    public LambdaQueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        LambdaQueryWrapper<Space> queryWrapper = new LambdaQueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), Space::getId, id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), Space::getUserId, userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), Space::getSpaceName, spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), Space::getSpaceLevel, spaceLevel);
// Handle sorting with proper null checks and field mapping
        // 假设 sortField 改成 "id,createTime" 这种格式
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = "ascend".equals(sortOrder);
            String[] fields = sortField.split(",");

            Map<String, SFunction<Space, ?>> fieldMap = Map.of(
                    "id", Space::getId,
                    "userId", Space::getUserId,
                    "spaceName", Space::getSpaceName,
                    "spaceLevel", Space::getSpaceLevel,
                    "createTime", Space::getCreateTime,
                    "editTime", Space::getEditTime,
                    "updateTime", Space::getUpdateTime
            );
            SFunction<Space, ?> column = fieldMap.get(sortField);
                if (column != null) {
                    queryWrapper.orderBy(true, isAsc, column);

            }
        }


        return queryWrapper;
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public void validSpace(Space space,boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值

        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        //创建时校验
        if(add){
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(spaceLevel==null, ErrorCode.PARAMS_ERROR, "空间等级不能为空");
        }
        //修改数据时，空间名称进行校验
        ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
        //修改数据时，空间名称校验
            ThrowUtils.throwIf(spaceName.length()>30, ErrorCode.PARAMS_ERROR, "空间名称过长");
            ThrowUtils.throwIf(spaceLevel!=null&&spaceLevelEnum==null,ErrorCode.PARAMS_ERROR,"空间等级不存在");
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum!=null){
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize()==null){
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount()==null){
                space.setMaxCount(maxCount);
            }
        }
    }
}




