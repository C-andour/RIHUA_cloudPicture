package com.itrihua.caritas.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    /**
     * 账号(邮箱)
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
    private String tempCode;


    private static final long serialVersionUID = 4891245735473120795L;
}
