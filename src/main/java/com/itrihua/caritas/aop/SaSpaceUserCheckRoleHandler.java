package com.itrihua.caritas.aop;

import cn.dev33.satoken.annotation.handler.SaAnnotationHandlerInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.itrihua.caritas.annotaion.SaSpaceUserCheckRole;
import com.itrihua.caritas.exception.BusinessException;
import com.itrihua.caritas.exception.ErrorCode;
import com.itrihua.caritas.exception.ThrowUtils;
import com.itrihua.caritas.manager.auth.SpaceUserAuthManager;
import com.itrihua.caritas.manager.auth.StpKit;
import com.itrihua.caritas.manager.auth.model.SpaceUserAuthContext;
import com.itrihua.caritas.manager.auth.model.SpaceUserPermission;
import com.itrihua.caritas.manager.auth.model.SpaceUserPermissionConstant;
import com.itrihua.caritas.model.entity.Picture;
import com.itrihua.caritas.model.entity.Space;
import com.itrihua.caritas.model.entity.SpaceUser;
import com.itrihua.caritas.model.entity.User;
import com.itrihua.caritas.model.enums.SpaceRoleEnum;
import com.itrihua.caritas.model.enums.SpaceTypeEnum;
import com.itrihua.caritas.service.PictureService;
import com.itrihua.caritas.service.SpaceService;
import com.itrihua.caritas.service.SpaceUserService;
import com.itrihua.caritas.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;

import static com.itrihua.caritas.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 注解 SaSpaceUserCheckRole 的处理器
 */
@Component
@Slf4j
public class SaSpaceUserCheckRoleHandler implements SaAnnotationHandlerInterface<SaSpaceUserCheckRole> {

    @Resource
    private SpaceUserPermissionFetcher spaceUserPermissionFetcher;

    /**
     * 本处理器,使用于被SaSpaceUserCheckRole注解的方法 / 类
     *
     * @return
     */
    @Override
    public Class<SaSpaceUserCheckRole> getHandlerAnnotationClass() {
        return SaSpaceUserCheckRole.class;
    }

    /**
     * 处理器
     *
     * @param at        空间用户注解
     * @param oldmethod 原方法
     */
    @Override
    public void checkMethod(SaSpaceUserCheckRole at, Method oldmethod) {
        //判断用户是否具有必要权限
        String mustPermission = at.mustPermission();
        // 从上下文对象中获取当前用户请求
        // 从当前用户请求中获取当前请求路径
        // 从当前请求路径中获取请求体,包括各类ID等等
        // 判断当前请求的方法,是公有/私有/团队请求
        // 从上述三大请求中,解析其方法所需要的权限集合
        // 校验当前用户是否满足权限需求
        Object loginId = StpKit.SPACE.getLoginId();
        String loginType = StpKit.SPACE.getLoginType();
        List<String> permissionList = spaceUserPermissionFetcher.getPermissionList(loginId, loginType);
        boolean hasPermission = permissionList.stream()
                .anyMatch(permission -> permission.equals(mustPermission));
        ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
    }


}
