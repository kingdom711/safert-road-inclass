package com.jinsung.safety_road_inclass.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass(JavaMailSender.class)
public class PasswordResetMailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.password-reset.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.password-reset.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String from;

    public void sendResetCode(String email, String code) {
        String resetUrl = frontendUrl.replaceAll("/$", "") + "/reset-password?email=" + email;

        if (!mailEnabled) {
            log.info("비밀번호 재설정 코드 발급: email={}, code={}, resetUrl={}", email, code, resetUrl);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("메일 발송 설정이 없어 비밀번호 재설정 코드를 발송하지 못했습니다: email={}", email);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (!from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(email);
        message.setSubject("[Safety Quest] 비밀번호 재설정 인증 코드");
        message.setText("""
                비밀번호 재설정 인증 코드입니다.

                인증 코드: %s
                유효 시간: 15분

                아래 화면에서 이메일, 인증 코드, 새 비밀번호를 입력해주세요.
                %s

                본인이 요청하지 않았다면 이 메일을 무시해주세요.
                """.formatted(code, resetUrl));

        mailSender.send(message);
    }
}
