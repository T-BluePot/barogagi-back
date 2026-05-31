package com.barogagi.push.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    public void sendMessage(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder().setToken(fcmToken).setNotification(
                                            Notification.builder().setTitle(title).setBody(body).build()).build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 발송 성공 response={}", response);

        } catch (Exception e) {
            log.error("FCM 발송 실패", e);
            throw new RuntimeException(e);
        }
    }
}
