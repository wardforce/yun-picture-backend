package com.wuzhenhua.yunpicturebackend.aop;

import com.wuzhenhua.yunpicturebackend.annotation.VipLevelCheck;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.enums.UserRoleEnum;
import com.wuzhenhua.yunpicturebackend.model.enums.UserVIPLevelEnum;
import com.wuzhenhua.yunpicturebackend.service.UserService;
import com.wuzhenhua.yunpicturebackend.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class VipLevelInterceptor {
    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint 切入点
     * @param vipLevelCheck 权限校验注解
     * @return
     */
    @Around("@annotation(vipLevelCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, VipLevelCheck vipLevelCheck) throws Throwable {
        String mustLevel = vipLevelCheck.mustLevel();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取当前登录用户
        User loginuser = userService.getLoginUser(request);
        UserVIPLevelEnum mustVIPEnum = UserVIPLevelEnum.getUserVIPLevelEnumByValue(mustLevel);
        if (mustVIPEnum == null) {
            return joinPoint.proceed();
        }
        //以下代码，必须有权限，才能通过
        UserVIPLevelEnum userVIPLevelEnum = UserVIPLevelEnum.getUserVIPLevelEnumByValue(loginuser.getVipLevel());
        ThrowUtils.throwIf(userVIPLevelEnum == null, ErrorCode.NO_AUTH_ERROR);

        //检查用户的VIP等级是否满足要求
        //如果用户是管理员，直接通过
        String userRole = loginuser.getUserRole();
        if (UserRoleEnum.ADMIN.getValue().equals(userRole)) {
            return joinPoint.proceed();
        }

        //根据等级层次检查：STANDARD < PRO < MAX
        //用户等级必须大于等于要求的等级
        boolean hasPermission = false;
        switch (mustVIPEnum) {
            case STANDARD:
                //要求standard，用户只要有任何VIP等级即可
                hasPermission = userVIPLevelEnum != null;
                break;
            case PRO:
                //要求pro，用户必须是PRO或MAX
                hasPermission = userVIPLevelEnum == UserVIPLevelEnum.PRO || userVIPLevelEnum == UserVIPLevelEnum.MAX;
                break;
            case MAX:
                //要求max，用户必须是MAX
                hasPermission = userVIPLevelEnum == UserVIPLevelEnum.MAX;
                break;
        }

        ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR, "VIP等级不足，需要" + mustVIPEnum.getText() + "及以上等级");
        return joinPoint.proceed();
    }
}
