package com.barogagi.push.service;

import com.barogagi.push.entity.PushToken;
import com.barogagi.push.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {

    private final PushTokenRepository pushTokenRepository;
    private final FcmService fcmService;

    public void sendToUser(
            String membershipNo,
            String title,
            String body
    ) {

        List<PushToken> tokens = pushTokenRepository.findByMembershipNoAndActiveYn(membershipNo, "Y");

        if (tokens.isEmpty()) {
            log.info("푸시 토큰 없음 userId={}", membershipNo);
            return;
        }

        for (PushToken pushToken : tokens) {
            try {
                fcmService.sendMessage(pushToken.getFcmToken(), title, body);

            } catch (Exception e) {
                log.error("푸시 발송 실패 token={}", pushToken.getFcmToken());
                pushToken.deactivate();
            }
        }
    }
}