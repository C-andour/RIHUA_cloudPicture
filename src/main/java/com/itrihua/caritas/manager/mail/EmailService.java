package com.itrihua.caritas.manager.mail;

import com.itrihua.caritas.exception.BusinessException;
import com.itrihua.caritas.exception.ErrorCode;
import com.itrihua.caritas.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.TimeUnit;

/**
 * 邮件发送服务
 */
@Slf4j
@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String my_mail;

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 发送文本邮件
    @Deprecated
    public void sendVerificationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(my_mail); // 必须与配置的username一致
        message.setTo(toEmail);
        message.setSubject("您的验证码");
        message.setText("您的验证码是：" + code + "，5分钟内有效");
        mailSender.send(message);
    }

    // 发送html,验证码文件
    public void sendHtmlEmail(String toEmail, String code) {
        // 发送验证码前的限制,发送大于三次,即限制
        String limitKey = "limit:" + toEmail;
        Long count = stringRedisTemplate.opsForValue().increment(limitKey);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(limitKey, 3, TimeUnit.MINUTES); // 3分钟内只能发1次
        }
        if (count > 3) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作过于频繁，请稍后再试");
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom(my_mail);
            helper.setTo(toEmail);
            helper.setSubject("验证码通知");
            String htmlContent = "<h1>您的验证码</h1>"
                    + "<p>验证码：<strong>" + code + "</strong></p>"
                    + "<p>有效期5分钟</p>";
            helper.setText(htmlContent, true);
            /**
             * 使用redis存储code,5分钟后清除
             */
            String key = "code:" + toEmail;
            ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
            valueOps.set(key, code, 5, TimeUnit.MINUTES);

            // 发送邮件
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送邮件失败");
        }
    }

    /**
     * 验证临时验证码是否正确
     */
    public void verifiyTempCode(String userEmail, String tempCode) {
        String key = "code:" + userEmail;
        String oldCode = stringRedisTemplate.opsForValue().get(key);
        if (!tempCode.equals(oldCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        // 验证成功,清除已有验证码
        stringRedisTemplate.delete(key);
    }

}
