package com.barogagi.batch.service;

import com.barogagi.batch.dto.SendDTO;
import com.barogagi.batch.entity.MessageOutbox;
import com.barogagi.batch.vo.SendResult;
import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.sendMessage.alimTalk.service.AlimTalkSendService;
import com.barogagi.sendMessage.email.dto.SendMailDTO;
import com.barogagi.sendMessage.email.service.EmailSendService;
import com.barogagi.sendMessage.sms.service.SmsSendService;
import com.barogagi.util.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MessageSendService {

    private final EncryptUtil encryptUtil;

    private final AlimTalkSendService alimTalkSendService;
    private final SmsSendService smsSendService;
    private final EmailSendService emailSendService;

    // 알림톡 / 문자
    private final String SERVICE_NAME = "핏플(fitpl)";
    private final String CANCEL_METHOD = "앱 접속 후 로그인";

    // 이메일
    private final String DIRECT_SEND_FROM;
    private final String SUBJECT = "[안내] 탈퇴 전환 안내 메일입니다.";

    public MessageSendService(Environment environment,
                              EncryptUtil encryptUtil,
                              AlimTalkSendService alimTalkSendService,
                              SmsSendService smsSendService,
                              EmailSendService emailSendService) {
        this.DIRECT_SEND_FROM = environment.getProperty("direct-send.from");
        this.encryptUtil = encryptUtil;
        this.alimTalkSendService = alimTalkSendService;
        this.smsSendService = smsSendService;
        this.emailSendService = emailSendService;
    }

    public SendResult send(MessageOutbox messageOutbox, UserMembershipInfo userMembershipInfo) {

        if (userMembershipInfo == null) {
            return new SendResult(false, "회원 정보 없음");
        }

        // 전화번호
        String tel = null;
        if (userMembershipInfo.getTel() != null) {
            tel = encryptUtil.decrypt(userMembershipInfo.getTel());
        }

        // 메시지에 들어갈 데이터 (공통)
        Map<String, String> variables = new HashMap<>();
        variables.put("serviceName", SERVICE_NAME);
        variables.put("withdrawDay", String.valueOf(userMembershipInfo.getDelDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
        variables.put("cancelMethod", CANCEL_METHOD);

        SendDTO sendDTO = new SendDTO();
        sendDTO.setTel(tel);
        sendDTO.setVariables(variables);

        try {
            switch (messageOutbox.getChannel()) {
                case ALIMTALK :
                    if (tel == null) return new SendResult(false, "알림톡 발송 대상 전화번호 없음");
                    boolean alimSuccess = alimTalkSendService.sendWithdrawalAlimTalk(sendDTO);
                    return alimSuccess ? new SendResult(true, null) : new SendResult(false, "알림톡 발송 실패");

                case SMS:
                    if (tel == null) return new SendResult(false, "SMS 발송 대상 전화번호 없음");
                    boolean smsSuccess = smsSendService.sendWithdrawalSMS(sendDTO);
                    return smsSuccess ? new SendResult(true, null) : new SendResult(false, "SMS 발송 실패");

                case EMAIL:
                    variables.put("supportEmail", "support@fitpl.com");
                    variables.put("companyKorName", "핏플");
                    variables.put("bizNumber", "000-00-00000");
                    variables.put("ceoName", "홍길동");
                    variables.put("address", "서울특별시 OO구 OO로 00");
                    variables.put("tel", "0000-0000");
                    variables.put("companyEngName", "Fitpl");
                    sendDTO.setVariables(variables);

                    SendMailDTO sendMailDTO = new SendMailDTO();
                    sendMailDTO.setFrom(DIRECT_SEND_FROM);
                    sendMailDTO.setTo(encryptUtil.decrypt(userMembershipInfo.getEmail()));
                    sendMailDTO.setSubject(SUBJECT);
                    sendDTO.setSendMailDTO(sendMailDTO);

                    boolean emailSuccess = emailSendService.sendWithdrawlEmail(sendDTO);
                    return emailSuccess ? new SendResult(true, null) : new SendResult(false, "이메일 발송 실패");
            }
        } catch (Exception e ) {
            log.error("메시지 발송 실패. channel={}, membershipNo={}", messageOutbox.getChannel(), messageOutbox.getMembershipNo(), e);
            return new SendResult(false, e.getMessage());
        }
        return new SendResult(false, "알 수 없는 채널");
    }
}
