package com.itrihua.caritas.controller;

import cn.hutool.core.util.StrUtil;
import com.itrihua.caritas.common.BaseResponse;
import com.itrihua.caritas.common.ResultUtils;
import com.itrihua.caritas.exception.ErrorCode;
import com.itrihua.caritas.exception.ThrowUtils;
import com.itrihua.caritas.manager.mail.CodeGeneratorUtil;
import com.itrihua.caritas.manager.mail.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/mail")
public class MailAuthController {

    @Resource
    private EmailService emailService;

    /**
     * 发送邮箱验证码
     * @param userAccount 用户邮箱
     */
    @GetMapping("/send-code")
    public BaseResponse<?> sendCode(@RequestParam String userAccount) {
        String code = CodeGeneratorUtil.generateVerificationCode();
        // 校验是否为正确的邮箱格式
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"; // 邮箱格式正则表达式
        ThrowUtils.throwIf(!(userAccount != null && userAccount.matches(regex)), ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        emailService.sendHtmlEmail(userAccount, code);
        return ResultUtils.success("验证码发送成功!");
    }

}
