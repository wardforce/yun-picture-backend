package com.wuzhenhua.yunpicturebackend.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.wuzhenhua.yunpicturebackend.model.dto.user.UserUpdatePasswordRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzhenhua.yunpicturebackend.constant.UserConstant;
import static com.wuzhenhua.yunpicturebackend.constant.UserConstant.USER_LOGIN_STATE;
import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.mapper.UserMapper;
import com.wuzhenhua.yunpicturebackend.model.dto.user.UserQueryRequest;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.enums.UserRoleEnum;
import com.wuzhenhua.yunpicturebackend.model.enums.UserVIPLevelEnum;
import com.wuzhenhua.yunpicturebackend.model.vo.LoginUserVO;
import com.wuzhenhua.yunpicturebackend.model.vo.UserVO;
import com.wuzhenhua.yunpicturebackend.service.UserService;
import com.wuzhenhua.yunpicturebackend.utils.ThrowUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ward
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-11-04 10:22:04
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 加密密码
     */
    @Override
    public String getEncodePassword(String password) {
        final String salt = "!y`dMA'L(}'r~*4.";
        return DigestUtils.md5DigestAsHex((salt + password).getBytes());
    }

    /**
     * 用户注册
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 用户确认密码
     * @return 用户id
     */
    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.检验参数
        if (StrUtil.isBlank(userAccount) || StrUtil.isBlank(userPassword) || StrUtil.isBlank(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "账号长度不能小于4");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        // 2.检查用户账号是否和数据库已知重复
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        Long count = this.baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号重复");
        // 3.密码加密
        String encodePassword = getEncodePassword(userPassword);
        // 4.插入数据到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encodePassword);
        user.setUserName("momo");
        boolean saveResult = this.save(user);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "用户注册失败");
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验
        if (StrUtil.isBlank(userAccount) || StrUtil.isBlank(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "账号错误");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码错误");
        // 2.加密
        String encodePassword = getEncodePassword(userPassword);
        // 3.查询数据库中的用户是否存在，找到这个用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        queryWrapper.eq(User::getUserPassword, encodePassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 不存在，抛出异常
        if (user == null) {
            log.info("用户不存在,用户名称与密码无法对应");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误或该用户被封禁");
        }
        // 5.保存用户的登录状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 返回脱敏用户数据
     *
     * @param user 用户
     * @return 脱敏用户数据
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 用户获取其他用户的数据
     *
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO UserVO = new UserVO();
        BeanUtils.copyProperties(user, UserVO);
        return UserVO;
    }

    /**
     * 获取脱敏后的用户类别
     *
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).toList();
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 登出用户
     *
     * @param request
     * @return
     */
    @Override
    public boolean userLogOut(HttpServletRequest request) {
        // 判断用户登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(userObj == null, ErrorCode.OPERATION_ERROR, "未登录");
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 分页查询（admin)
     *
     * @param userQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<User> getUserQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        Long vipNumber = userQueryRequest.getVipNumber();
        String vipLevel = userQueryRequest.getVipLevel();
        Long inviteUser = userQueryRequest.getInviteUser();
        String shareCode = userQueryRequest.getShareCode();
        Long phoneNumber = userQueryRequest.getPhoneNumber();
        String vipCode = userQueryRequest.getVipCode();
        String email = userQueryRequest.getEmail();
        String phoneCountryCode = userQueryRequest.getPhoneCountryCode();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), User::getId, id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), User::getUserRole, userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), User::getUserAccount, userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), User::getUserName, userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), User::getUserProfile, userProfile);
        queryWrapper.like(StrUtil.isNotBlank(email), User::getEmail, email);
        queryWrapper.eq(ObjUtil.isNotNull(inviteUser), User::getInviteUser, inviteUser);
        queryWrapper.eq(StrUtil.isNotBlank(shareCode), User::getShareCode, shareCode);
        queryWrapper.eq(ObjUtil.isNotNull(vipNumber), User::getVipNumber, vipNumber);
        queryWrapper.eq(StrUtil.isNotBlank(vipCode), User::getVipCode, vipCode);
        // phoneCountryCode and phoneNumber should be queried together
        if (ObjUtil.isNotNull(phoneNumber) && StrUtil.isNotBlank(phoneCountryCode)) {
            queryWrapper.eq(User::getPhoneCountryCode, phoneCountryCode)
                    .eq(User::getPhoneNumber, phoneNumber);
        }
        queryWrapper.eq(StrUtil.isNotBlank(vipLevel), User::getVipLevel, vipLevel);
        queryWrapper.eq(StrUtil.isNotBlank(email), User::getEmail, email);
        // 安全的排序处理：仅允许白名单字段，避免 LambdaQueryWrapper 传入字符串导致的错误以及 SQL 注入风险
        if (StrUtil.isNotBlank(sortField)) {
            // 兼容前端常见的排序关键字：asc/desc 和 ascend/descend
            boolean asc = "ascend".equalsIgnoreCase(sortOrder) || "asc".equalsIgnoreCase(sortOrder);
            // 将可排序的前端字段映射到实体的 Getter（SFunction）
            java.util.Map<String, com.baomidou.mybatisplus.core.toolkit.support.SFunction<User, ?>> sortMap = new java.util.HashMap<>();
            sortMap.put("id", User::getId);
            sortMap.put("userAccount", User::getUserAccount);
            sortMap.put("userName", User::getUserName);
            sortMap.put("userRole", User::getUserRole);
            sortMap.put("vipNumber", User::getVipNumber);
            sortMap.put("vipLevel", User::getVipLevel);
            sortMap.put("email", User::getEmail);
            sortMap.put("phoneNumber", User::getPhoneNumber);
            sortMap.put("phoneCountryCode", User::getPhoneCountryCode);
            // 时间字段
            sortMap.put("inviteUser", User::getInviteUser);
            sortMap.put("createTime", User::getCreateTime);
            sortMap.put("updateTime", User::getUpdateTime);
            sortMap.put("editTime", User::getEditTime);
            sortMap.put("vipExpireTime", User::getVipExpireTime);
            sortMap.put("inviteUser", User::getInviteUser);
            sortMap.put("shareCode", User::getShareCode);
            sortMap.put("userProfile", User::getUserProfile);
            com.baomidou.mybatisplus.core.toolkit.support.SFunction<User, ?> column = sortMap.get(sortField);
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

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public boolean isVIP(User user) {
        return user != null && UserRoleEnum.VIP.getValue().equals(user.getUserRole());
    }

    @Override
    public boolean isVipPro(User user) {
        return user != null && UserRoleEnum.VIP.getValue().equals(user.getUserRole())
        &&(UserVIPLevelEnum.PRO.getValue().equals(user.getVipLevel())||
        UserVIPLevelEnum.MAX.getValue().equals(user.getVipLevel()))&& isVipExpired(user);
    }

    @Override
    public boolean isVipMAX(User user) {
        return user != null && UserRoleEnum.VIP.getValue().equals(user.getUserRole())
        &&UserVIPLevelEnum.MAX.getValue().equals(user.getVipLevel())&& isVipExpired(user);
    }

    @Override
    public boolean isVipStandard(User user) {
        // TODO Auto-generated method stub
        return user != null && UserRoleEnum.VIP.getValue().equals(user.getUserRole())
        &&(UserVIPLevelEnum.STANDARD.getValue().equals(user.getVipLevel())||
        UserVIPLevelEnum.PRO.getValue().equals(user.getVipLevel())||
        UserVIPLevelEnum.MAX.getValue().equals(user.getVipLevel()))&& isVipExpired(user);
    }

    /**
     * 判断会员是否已过期
     * 规则：vipExpireTime 为空返回 true；当前时间早于 vipExpireTime 返回 false；当前时间等于或晚于 vipExpireTime 返回 true。
     * 使用 Java 8 时间 API 并记录关键日志。
     *
     * @param user 用户
     * @return 是否过期
     */
    @Override
    public boolean isVipExpired(User user) {
        if (user == null) {
            log.warn("isVipExpired user is null");
            return true;
        }
        Date expire = user.getVipExpireTime();
        if (expire == null) {
            log.info("isVipExpired userId={}, expireTime=null -> expired=true", user.getId());
            return true;
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime expireTime = expire.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        boolean expired = !now.isBefore(expireTime);
        log.info("isVipExpired userId={}, now={}, expireTime={}, expired={}", user.getId(), now, expireTime, expired);
        return expired;
    }

    @Override
    public boolean userUpdatePassword(UserUpdatePasswordRequest userUpdatePasswordRequest, HttpServletRequest request) {
        String userOldPassword = userUpdatePasswordRequest.getUserOldPassword();
        String userNewPassword = userUpdatePasswordRequest.getUserNewPassword();
        String checkPassword = userUpdatePasswordRequest.getCheckPassword();

        // 1.参数校验（快速失败）
        if (StrUtil.isBlank(userOldPassword) || StrUtil.isBlank(userNewPassword) || StrUtil.isBlank(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        ThrowUtils.throwIf(userNewPassword.length() < 8 || checkPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "用户密码长度不足");
        ThrowUtils.throwIf(!userNewPassword.equals(checkPassword),
                ErrorCode.PARAMS_ERROR, "用户密码输入不一致");

        // 2.检验用户登录
        User loginUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        // 3.加密密码
        String oldPasswordEncoded = getEncodePassword(userOldPassword);
        String newPasswordEncoded = getEncodePassword(userNewPassword);

        // 4.链式更新（原子操作：验证旧密码并更新新密码）
        boolean result = this.lambdaUpdate()
                .set(User::getUserPassword, newPasswordEncoded)
                .eq(User::getId, loginUser.getId())
                .eq(User::getUserPassword, oldPasswordEncoded)
                .update();

        ThrowUtils.throwIf(!result, ErrorCode.PARAMS_ERROR, "旧密码错误或密码未变更");

        return true;
    }

}
