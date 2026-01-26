package com.barogagi.sendMessage.alimTalk.service;

import com.barogagi.sendMessage.alimTalk.PurpleBookClient;
import com.barogagi.sendMessage.alimTalk.dto.PurpleBookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AlimTalkSendService {

    private final PurpleBookClient purpleBookClient;

    private final String SERVICE_NAME;

    private final String WITHDRAWAL_TIME = "24";
    private final String cancelMethod = "앱 접속 후 로그인";

    public AlimTalkSendService(Environment environment,
                               PurpleBookClient purpleBookClient) {
        this.SERVICE_NAME = environment.getProperty("service.name");
        this.purpleBookClient = purpleBookClient;
    }

    public boolean sendWithdrawalAlimTalk(String phone) {

        Map<String, String> variables = Map.of("serviceName", SERVICE_NAME,
                                                "afterHours", WITHDRAWAL_TIME,
                                                "cancelMethod", cancelMethod);
        PurpleBookResponse response = purpleBookClient.withdrawal24hAlimTalk(phone, variables);
        if (response.isSuccess()) {
            return true;
        }

        // 알림톡 실패 → SMS fallback
        return purpleBookClient.sendSms(phone,buildSmsMessage());
    }

    private String buildSmsMessage() {
        return SERVICE_NAME + "\n" +
                "회원 탈퇴가 " + WITHDRAWAL_TIME +"시간 후 확정됩니다.\n" +
                "탈퇴 확정 시 서비스 이용이 제한되며, 계정 정보는 관련 법령에 따라 처리되어 이후 복구가 불가능합니다.\n" +
                "탈퇴를 원하지 않으실 경우 확정 전까지 " + cancelMethod +"을 통해 탈퇴 취소가 가능합니다.";
    }
}
