package com.itrihua.caritas.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 空间用户权限校验,权限列表在 resource/biz/spaceUserAuthConfig.json
 * <p> 可标注在函数、类上（效果等同于标注在此类的所有方法上） 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE})
public @interface SaSpaceUserCheckRole {

    /**
     * 对于空间用户的权限校验,默认为"",无权限
     * @return
     */
    String mustPermission() default "";

}
