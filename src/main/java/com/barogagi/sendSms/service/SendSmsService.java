package com.barogagi.sendSms.service;

import com.barogagi.sendSms.dto.SendSmsVO;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SendSmsService {

    private static final Logger logger = LoggerFactory.getLogger(SendSmsService.class);

    /**
     * SMS 발송
     * @param sendSmsVO
     * @return boolean
     */
    public boolean sendSms(SendSmsVO sendSmsVO){

        boolean result = true;

        String sendTel = "01022581349";
        String apiKey = "NCSLFRXVINKTJJRF";
        String apiSecretKey = "FVIG5UM7784HPLYECNCSYF2FVRXVCBWR";

        DefaultMessageService messageService =  NurigoApp.INSTANCE.initialize(apiKey, apiSecretKey, "https://api.solapi.com");
        // Message 패키지가 중복될 경우 net.nurigo.sdk.message.model.Message로 치환하여 주세요
        Message message = new Message();
        message.setFrom(sendTel);
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
}
