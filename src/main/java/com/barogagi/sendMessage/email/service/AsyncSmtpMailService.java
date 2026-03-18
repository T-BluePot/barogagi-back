package com.barogagi.sendMessage.email.service;

import com.barogagi.sendMessage.email.dto.SendMailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Slf4j
@Service
public class AsyncSmtpMailService {

    private final String SMTP_HOST = "smtp.gmail.com";
    private final int SMTP_PORT = 587;
    private final String USERNAME; // Gmail 계정
    private final String PASSWORD;   // 앱 비밀번호

    private final int MAX_RETRY = 3;

    public AsyncSmtpMailService(Environment environment) {
        USERNAME = environment.getProperty("direct-send.from");
        PASSWORD = environment.getProperty("app.password");
    }

    @Async
    public void sendMailAsync(SendMailDTO sendMailDTO) {
        boolean result = sendWithRetry(sendMailDTO.getFrom(), sendMailDTO.getTo(), sendMailDTO.getSubject(), sendMailDTO.getBody(), MAX_RETRY);
        if(result) {
            log.info("Mail sent successfully to {}", sendMailDTO.getTo());
        } else {
            log.error("Mail failed after retries to {}", sendMailDTO.getTo());
        }
    }

    private boolean sendWithRetry(String from, String to, String subject, String body, int retriesLeft) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            return true;

        } catch (MessagingException e) {
            if(retriesLeft > 0) {
                log.info("Retrying mail to {}, retries left: {}", to, (retriesLeft - 1));
                return sendWithRetry(from, to, subject, body, retriesLeft - 1);
            }
            return false;
        }
    }
}