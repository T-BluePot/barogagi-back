package com.barogagi.sendMessage.sms.service;

import com.barogagi.batch.dto.SendDTO;
import com.barogagi.sendMessage.sms.dto.SendSmsVO;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SmsSendService {

    private static final Logger logger = LoggerFactory.getLogger(SmsSendService.class);

    private final String SEND_TEL;
    private final String API_KEY;
    private final String API_SECRET_KEY;
    private final DefaultMessageService messageService;

    public SmsSendService(Environment environment) {
        this.SEND_TEL = environment.getRequiredProperty("purplebook.tel");
        this.API_KEY = environment.getRequiredProperty("purplebook.api-key");
        this.API_SECRET_KEY = environment.getRequiredProperty("purplebook.api-secret-key");
        this.messageService = NurigoApp.INSTANCE.initialize(API_KEY, API_SECRET_KEY, "https://api.solapi.com");
    }

    /**
     * SMS 발송
     * @param sendSmsVO
     * @return boolean
     */
    public boolean sendSms(SendSmsVO sendSmsVO){

        boolean result = true;

        Message message = new Message();
        message.setFrom(SEND_TEL);
        message.setTo(sendSmsVO.getRecipientTel());
        message.setText(sendSmsVO.getMessageContent());

        try {
            // send 메소드로 ArrayList<Message> 객체를 넣어도 동작합니다!
            messageService.send(message);
            result = true;

        } catch (NurigoMessageNotReceivedException exception) {
            // 발송에 실패한 메시지 목록을 확인할 수 있습니다!
            logger.info("발송에 실패한 메시지 목록: {}", exception.getFailedMessageList());
            logger.error(exception.getMessage());
            result = false;

        } catch (Exception exception) {
            logger.error(exception.getMessage());
            result = false;
        }

        return result;
    }
  
    public boolean sendWithdrawalSMS(SendDTO sendDTO) {
        String message = buildWithdrawalSMSMessage(sendDTO.getVariables());

        SendSmsVO sendSmsVO = new SendSmsVO();
        sendSmsVO.setRecipientTel(sendDTO.getTel());
        sendSmsVO.setMessageContent(message);
        return this.sendSms(sendSmsVO);
    }

    public String buildWithdrawalSMSMessage(Map<String, String> variables) {
        return String.format("안녕하세요, %s 입니다.\n" +
                        "\n" +
                        "고객님의 탈퇴 신청이 접수되어 계정이 탈퇴 상태로 전환될 예정입니다.\n" +
                        "\n" +
                        "■ 탈퇴 전환 예정일 : %s\n" +
                        "\n" +
                        "탈퇴 전환 이후에는 서비스 이용이 제한되며, 계정 정보는 관련 법령에 따라 처리됩니다.\n" +
                        "\n" +
                        "탈퇴를 원하지 않으실 경우, 전환 전까지 %s을 통해 탈퇴를 취소하실 수 있습니다.",
                variables.get("serviceName"), variables.get("afterHours"), variables.get("cancelMethod"));
    }
}
