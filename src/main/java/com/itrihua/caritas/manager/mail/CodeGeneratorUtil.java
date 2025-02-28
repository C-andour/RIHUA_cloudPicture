package com.itrihua.caritas.manager.mail;

import java.util.Random;

/**
 * 生成随机验证码工具类
 */
public class CodeGeneratorUtil {
    public static String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }
}
