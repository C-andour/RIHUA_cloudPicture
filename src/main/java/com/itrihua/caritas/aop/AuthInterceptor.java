package com.itrihua.caritas.aop;

import com.itrihua.caritas.annotaion.AuthCheck;
import com.itrihua.caritas.exception.BusinessException;
import com.itrihua.caritas.exception.ErrorCode;
import com.itrihua.caritas.model.entity.User;
import com.itrihua.caritas.model.enums.UserRoleEnum;
import com.itrihua.caritas.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Slf4j
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     * @param joinPoint 切入点(原方法)
     * @param authCheck 权限校验注解
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        /**
         * 此处为非Servlet层代码(普通类,工具类,静态方法,异步任务),直接获取HttpServletRequest对象比较困难
         * 因此使用上下文的方式对它进行读取,但需要注意，HttpServletRequest 的输入流（InputStream）是单向的，<strong>默认只能被读取一次</strong>
         *
         * 流程为:  1. 从Spring的线程上下文获取当前请求的属性
         *         2. 转换为Servlet实现类，并提取HttpServletRequest对象
         */
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 不需要权限，放行
        if (mustRoleEnum == null) {
            log.info("该方法不需要管理员权限");
            return joinPoint.proceed();
        }
        // ------------必须有必要权限才能通过----------
        // 获取当前用户具有的权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 当前用户没有权限，拒绝
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 要求必须有管理员权限，但用户没有管理员权限，拒绝
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

