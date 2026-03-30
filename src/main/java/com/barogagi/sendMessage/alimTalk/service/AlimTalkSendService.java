package com.barogagi.sendMessage.alimTalk.service;

import com.barogagi.batch.dto.SendDTO;
import com.barogagi.sendMessage.alimTalk.client.SolapiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlimTalkSendService {

    private final SolapiClient solapiClient;

    @Value("${solapi.preWithdrawal.template-id}")
    private String preWithdrawalTemplateId;

    public boolean sendWithdrawalAlimTalk(SendDTO sendDTO) {

        try {
            // 1. SolapiClient 호출
            sendDTO.setTemplateId(preWithdrawalTemplateId);
            String response = solapiClient.sendAlimTalk(sendDTO);

            // 2. 성공 여부 체크
            return response != null && response.contains("groupId");

        } catch (Exception e) {
            log.error("알림톡 발송 실패", e);
            return false;
        }
    }
}
