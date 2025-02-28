package com.itrihua.caritas.model.dto.user;

import lombok.Data;

/**
 * 重置密码请求
 */
@Data
public class UserResetPasswordRequest {

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 临时验证码
     */
    private String resetCode;
}
