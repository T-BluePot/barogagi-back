package com.barogagi.sendSms.service;

import com.barogagi.sendSms.dto.SendSmsVO;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class SendSmsService {

    private static final Logger logger = LoggerFactory.getLogger(SendSmsService.class);

    private final String SEND_TEL;
    private final String API_KEY;
    private final String API_SECRET_KEY;
    private final DefaultMessageService messageService;

    public SendSmsService(Environment environment) {
        this.SEND_TEL = environment.getRequiredProperty("send.sms.tel");
        this.API_KEY = environment.getRequiredProperty("send.sms.api-key");
        this.API_SECRET_KEY = environment.getRequiredProperty("send.sms.api-secret-key");
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
}
