package com.barogagi.sendMessage.alimTalk.service;

import com.barogagi.batch.dto.SendDTO;
import com.barogagi.sendMessage.alimTalk.client.SolapiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlimTalkSendService {

    private final SolapiClient solapiClient;
    private final Environment environment;

    @Value("${solapi.preWithdrawal.template-id}")
    private String preWithdrawalTemplateId;

    public boolean sendWithdrawalAlimTalk(SendDTO sendDTO) {

        try {

            if(isProd()) {  // 실서버에서만 알림톡 발송 가능
                // 1. SolapiClient 호출
                sendDTO.setTemplateId(preWithdrawalTemplateId);
                String response = solapiClient.sendAlimTalk(sendDTO);

                // 2. 성공 여부 체크
                return response != null && response.contains("groupId");

            } else {  // 개발/테스트 서버에서는 항상 성공 처리
                return true;
            }

        } catch (Exception e) {
            log.error("알림톡 발송 실패", e);
            return false;
        }
    }

    private boolean isProd() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}
