package com.wuzhenhua.yunpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuzhenhua.yunpicturebackend.model.dto.space.analyze.*;
import com.wuzhenhua.yunpicturebackend.model.entity.Space;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.vo.space.analyze.*;

import java.util.List;

public interface SpaceAnalyzeService extends IService<Space> {
    /**
     * Get space usage analyze
     *
     * @param user
     * @param spaceAnalyzeRequest
     * @return
     */
    SpaceUsageAnalyzeRequest getSpaceUsageAnalyze(User user, SpaceAnalyzeRequest spaceAnalyzeRequest);

    /**
     * Get space category analyze
     * @param spaceCategoryAnalyzeRequest
     * @param loginUser
     * @return
     */
     List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);
    /**
     * 获取空间图片标签分析结果
      * @param spaceTagAnalyzeRequest
     * @param loginUser
     * @return List<SpaceTagAnalyzeResponse>
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);
    /**
     * 获取空间图片大小分析结果
      * @return List<SpaceTagAnalyzeResponse>
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 空间用户获取空间行为行为
     * @param spaceUserAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);
}
