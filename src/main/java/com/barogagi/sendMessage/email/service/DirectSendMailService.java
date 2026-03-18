package com.barogagi.sendMessage.email.service;

import com.barogagi.sendMessage.email.dto.SendMailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Properties;

@Slf4j
@Service
public class DirectSendMailService {

    /**
     * Direct Send 방식으로 메일 발송
     * SendMailDTO
         * from 발신자 이메일 (yourdomain.com@yourdomain.com)
         * to 수신자 이메일
         * subject 메일 제목
         * body 메일 내용
     * @throws Exception
     */
    public void sendDirectMail(SendMailDTO sendMailDTO) throws Exception {
        // 1. 수신 도메인 MX 레코드 조회
        String recipientDomain = sendMailDTO.getTo().split("@")[1];
        DirContext ictx = new InitialDirContext();
        Attributes attrs = ictx.getAttributes("dns:/" + recipientDomain, new String[]{"MX"});
        Attribute attr = attrs.get("MX");

        String mxHost;
        if (attr != null) {
            String mxRecord = (String) attr.get(0);
            mxHost = mxRecord.substring(mxRecord.indexOf(" ") + 1).trim();
            System.out.println("MX Host for " + recipientDomain + ": " + mxHost);
        } else {
            mxHost = recipientDomain;
            System.out.println("No MX record found. Using domain: " + mxHost);
        }

        // 2. JavaMail Properties 설정
        Properties props = new Properties();
        props.put("mail.smtp.host", mxHost);
        props.put("mail.smtp.port", "25");  // Direct Send 기본 포트
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");

        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sendMailDTO.getFrom()));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(sendMailDTO.getTo()));
        message.setSubject(sendMailDTO.getSubject());
        message.setText(sendMailDTO.getBody());

        // 3. 메일 발송
        try {
            Transport.send(message);
            log.info("Mail sent successfully via Direct Send!");
        } catch (SendFailedException sfe) {
            log.error("Send failed: {}", sfe.getMessage());
        }
    }
}
