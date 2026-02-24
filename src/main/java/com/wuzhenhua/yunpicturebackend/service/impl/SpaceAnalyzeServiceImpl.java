package com.wuzhenhua.yunpicturebackend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.mapper.SpaceMapper;
import com.wuzhenhua.yunpicturebackend.model.dto.space.analyze.*;
import com.wuzhenhua.yunpicturebackend.model.entity.Picture;
import com.wuzhenhua.yunpicturebackend.model.entity.Space;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.vo.space.analyze.*;
import com.wuzhenhua.yunpicturebackend.service.PictureService;
import com.wuzhenhua.yunpicturebackend.service.SpaceAnalyzeService;
import com.wuzhenhua.yunpicturebackend.service.SpaceService;
import com.wuzhenhua.yunpicturebackend.service.UserService;
import com.wuzhenhua.yunpicturebackend.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wuzhenhua
 * @create
 */
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceAnalyzeService {
    @Resource
    UserService userService;
    @Resource
    SpaceService spaceService;
    @Resource
    PictureService pictureService;

    /**
     *
     * 校验用户是否有权限分析指定空间
     * @param spaceAnalyzeRequest
     * @param loginUser
     */
    public void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        //全空间分析或公共图库，仅管理员可以看到
        if(queryPublic && queryAll) {
            Space space = this.getById(spaceId);
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR,"只有管理员才能分析私密空间");
        }else{
            //分析指定空间,仅本人和管理员可以访问
            ThrowUtils.throwIf(spaceId==null, ErrorCode.PARAMS_ERROR,"空间ID不能为空");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null, ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            spaceService.checkSpaceAuth(loginUser, space);
        }
    }
    /**
     * 根据请求参数填充空间分析的范围过滤条件
     * 优先级：全空间 (queryAll) > 公共图库 (queryPublic) > 指定空间 (spaceId)
     *
     * @param spaceAnalyzeRequest 空间分析请求
     * @param pictureLambdaQueryWrapper 图片查询包装器
     */
    private void fillSpaceBySpaceLevel(SpaceAnalyzeRequest spaceAnalyzeRequest, LambdaQueryWrapper<Picture> pictureLambdaQueryWrapper) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        //全空间分析或公共图库
        if (queryAll) return;
        if (queryPublic) {
            pictureLambdaQueryWrapper.isNull(Picture::getSpaceId);
            return;
        }
        //分析指定空间
        if (spaceId != null) {
            pictureLambdaQueryWrapper.eq(Picture::getSpaceId, spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.FORBIDDEN_ERROR,"未指定任何逻辑");
    }
    private static void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        if (spaceAnalyzeRequest.isQueryAll()) {
            return;
        }
        if (spaceAnalyzeRequest.isQueryPublic()) {
            queryWrapper.isNull("spaceId");
            return;
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }


    @Override
    public SpaceUsageAnalyzeRequest getSpaceUsageAnalyze(User loginUser, SpaceAnalyzeRequest spaceAnalyzeRequest) {
        //校验参数
        //全空间或公共图库，需要从Picture表中查询
        if (spaceAnalyzeRequest.isQueryAll()|| spaceAnalyzeRequest.isQueryPublic()){
            //校验，仅仅管理员可以使用
            checkSpaceAnalyzeAuth(spaceAnalyzeRequest, loginUser);
            //统计图库的使用空间
            LambdaQueryWrapper<Picture> pictureLambdaQueryWrapper = new LambdaQueryWrapper<>();
            pictureLambdaQueryWrapper.select(Picture::getPicSize);
            //补充查询范围
            fillSpaceBySpaceLevel(spaceAnalyzeRequest, pictureLambdaQueryWrapper);
            List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(pictureLambdaQueryWrapper);
            long usedSize = pictureObjList.stream().mapToLong(result -> result instanceof Long ? (Long) result : 0).sum();
            long usedCount = pictureObjList.size();
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(usedSize);
            //公共图库没有空间限制，私密空间的空间限制从Space表中查询
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
            spaceUsageAnalyzeResponse.setUsedCount(usedCount);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setCountUsageRatio(null);
        }else{
            //分析特定空间，需要从Space表中查询
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId==null||spaceId<=0, ErrorCode.PARAMS_ERROR,"空间ID不能为空");
            //获取空间信息
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null, ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(space.getTotalSize());
            //公共图库没有空间限制，私密空间的空间限制从Space表中查询
            spaceUsageAnalyzeResponse.setMaxSize(space.getMaxSize());
            double sizeUsageRatio = NumberUtil.round( space.getTotalSize() * 100.0 / space.getMaxSize(),2).doubleValue();
            double countUsageRatio = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount() ,2).doubleValue();
            spaceUsageAnalyzeResponse.setSizeUsageRatio(sizeUsageRatio);
            spaceUsageAnalyzeResponse.setUsedCount(space.getTotalCount());
            spaceUsageAnalyzeResponse.setMaxCount(space.getMaxCount());
            spaceUsageAnalyzeResponse.setCountUsageRatio(countUsageRatio);
        }

        return null;
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest==null, ErrorCode.PARAMS_ERROR,"请求参数不能为空");
         //校验权限
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
        //构造查询条件
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, pictureQueryWrapper);
        //使用Mybatis-Plus的分组查询
        pictureQueryWrapper.select("category", "COUNT(*) as count", "SUM(picSize) as totalSize")
                .groupBy("category");
        //查询
        return pictureService.getBaseMapper().selectMaps(pictureQueryWrapper).stream()
                .map(result->{
                    String category = (String) result.get("category");
                    Long count = (Long) result.get("count");
                    Long totalSize = (Long) result.get("totalSize");
                    SpaceCategoryAnalyzeResponse categoryData = new SpaceCategoryAnalyzeResponse();
                    categoryData.setCategory(category);
                    categoryData.setCount(count);
                    categoryData.setTotalSize(totalSize);
                    return categoryData;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest==null, ErrorCode.PARAMS_ERROR,"请求参数不能为空");
        //校验权限
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);
        //构造查询条件
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<Picture> pictureLambdaQueryWrapper = new LambdaQueryWrapper<>();
        fillSpaceBySpaceLevel(spaceTagAnalyzeRequest, pictureLambdaQueryWrapper);
        pictureLambdaQueryWrapper.select(Picture::getTags);
        List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(pictureLambdaQueryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());
        //得到扁平的结构
        Map<String, Long> TagCollect = pictureObjList.stream()
                .flatMap(tagsJson -> JSONUtil.toList(tagsJson.toString(), String.class).stream())
                .filter(ObjUtil::isNotNull)
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
        //转换为响应对象列表，按照使用次数进行排序
        return TagCollect.entrySet().stream()
                .sorted((e1,e2)->Long.compare(e2.getValue(), e1.getValue()))
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest==null, ErrorCode.PARAMS_ERROR,"请求参数不能为空");
        //校验权限
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);
        //构造查询条件
        LambdaQueryWrapper<Picture> pictureQueryWrapper = new LambdaQueryWrapper<>();
        fillSpaceBySpaceLevel(spaceSizeAnalyzeRequest, pictureQueryWrapper);
        //查询所有符合条件的图片大小，进行分组统计
        pictureQueryWrapper.select(Picture::getPicSize);
        List<Long> pictureSizeList = pictureService.getBaseMapper().selectObjs(pictureQueryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(size -> (Long) size)
                .toList();
        //定义分段范围，注意使用有序的map
        Map<String,Long> sizeRanges= new LinkedHashMap<>();
        sizeRanges.put("<100KB", pictureSizeList.stream().filter(size -> size < 100 * 1024).count());
        sizeRanges.put("100KB-500KB", pictureSizeList.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
        sizeRanges.put("500KB-1MB", pictureSizeList.stream().filter(size -> size >= 500 * 1024 && size < 1 * 1024 * 1024).count());
        sizeRanges.put(">1MB", pictureSizeList.stream().filter(size -> size >= 1 * 1024 * 1024).count());
        return sizeRanges.entrySet().stream()
                .map(entry->new SpaceSizeAnalyzeResponse(entry.getKey(),entry.getValue()))
                .collect(Collectors.toList());

    }

    @Override
    public List<SpaceUserAnalyzeRequest> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest==null, ErrorCode.PARAMS_ERROR,"请求参数不能为空");
        //校验权限
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);
        //构造查询条件
        LambdaQueryWrapper<Picture> pictureQueryWrapper = new LambdaQueryWrapper<>();
        fillSpaceBySpaceLevel(spaceUserAnalyzeRequest, pictureQueryWrapper);
        //补充用户id查询
        Long userId = spaceUserAnalyzeRequest.getUserId();
        pictureQueryWrapper.eq(ObjUtil::isNotNull(userId),,loginUser.getId());
        return List.of();
    }

}