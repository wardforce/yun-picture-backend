package com.wuzhenhua.yunpicturebackend.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wuzhenhua.yunpicturebackend.model.dto.user.UserQueryRequest;
import com.wuzhenhua.yunpicturebackend.model.dto.user.UserUpdatePasswordRequest;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.vo.LoginUserVO;
import com.wuzhenhua.yunpicturebackend.model.vo.UserVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author ward
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-11-04 10:22:04
 */
public interface UserService extends IService<User> {

    /**
     * @return 加密密码
     */
    String getEncodePassword(String password);

    Long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取脱敏后的登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 普通用户获得登录后的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 返回脱敏用户列表
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     *
     * @param request
     * @return 当前用户登录信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户登出
     *
     * @param request
     * @return
     */
    boolean userLogOut(HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    LambdaQueryWrapper<User> getUserQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 是否为会员
     *
     * @param user
     * @return
     */
    boolean isVIP(User user);

    /**
     * 是否为会员PRO
     *
     * @param user
     * @return
     */
    boolean isVipPro(User user);

    /**
     * 是否为会员MAX
     *
     * @param user
     * @return
     */
    boolean isVipMAX(User user);
    /**
     * 是否为会员Standard
     *
     * @param user
     * @return
     */
    boolean isVipStandard(User user);
    /**
     * 是否为会员过期
     *
     * @param user
     * @return
     */
    boolean isVipExpired(User user);

    /**
     * 用户修改密码
     *
     * @param
     * @param request
     * @return
     */boolean userUpdatePassword(UserUpdatePasswordRequest userUpdatePasswordRequest, HttpServletRequest request);
}
