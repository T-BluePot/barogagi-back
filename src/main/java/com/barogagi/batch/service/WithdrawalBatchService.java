package com.barogagi.batch.service;

import com.barogagi.member.domain.MembershipStatus;
import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.repository.UserMembershipRepository;
import com.barogagi.sendMessage.alimTalk.service.AlimTalkSendService;
import com.barogagi.sendMessage.email.dto.SendMailDTO;
import com.barogagi.sendMessage.email.service.EmailSendService;
import com.barogagi.util.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WithdrawalBatchService {

    private final EncryptUtil encryptUtil;
    private final AlimTalkSendService alimTalkSendService;
    private final EmailSendService emailSendService;
    private final UserMembershipRepository userMembershipRepository;

    // 알림톡 / 문자
    private final String SERVICE_NAME = "핏플(fitpl)";
    private final String AFTER_HOURS = "24";
    private final String CANCEL_METHOD = "앱 접속 후 로그인";

    // 이메일
    private final String DIRECT_SEND_FROM;
    private final String SUBJECT = "[안내] 탈퇴 전환 안내 메일입니다.";

    public WithdrawalBatchService(Environment environment,
                                  EncryptUtil encryptUtil,
                                  AlimTalkSendService alimTalkSendService,
                                  EmailSendService emailSendService,
                                  UserMembershipRepository userMembershipRepository) {
        this.DIRECT_SEND_FROM = environment.getProperty("direct-send.from");
        this.encryptUtil = encryptUtil;
        this.alimTalkSendService = alimTalkSendService;
        this.emailSendService = emailSendService;
        this.userMembershipRepository = userMembershipRepository;
    }

    @Transactional
    public void processWithdrawlBatch() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 탈퇴 적용 대상 존재 여부 체크
        if(!userMembershipRepository.existsWithdrawalTarget(MembershipStatus.WITHDRAWAL_PENDING, now)) {
            return;
        }

        // 2. DELETED_MEMBERSHIP_INFO에 저장
        int inserted = userMembershipRepository.insertDeletedMembers(now);

        // 3. 원본 테이블 삭제
        int deleted = userMembershipRepository.deleteWithdrawnMembers(now);

        log.info("[ WithdrawalBatchService.processWithdrawlBatch() ] inserted={}, deleted={}", inserted, deleted);
    }

    @Transactional
    public void processBeforeWithdrawlBatch() {
        LocalDateTime dateTime = LocalDateTime.now().plusHours(24);

        // 1. 24시간 이후 탈퇴 적용 대상 존재 여부 체크
        if(!userMembershipRepository.existsWithdrawalTarget(MembershipStatus.WITHDRAWAL_PENDING, dateTime)) {
            return;
        }

        // 2. 문자/알림톡 발송 대상자 조회
        List<UserMembershipInfo> withdrawlList = userMembershipRepository.findWithdrawalScheduledAfter(MembershipStatus.WITHDRAWAL_PENDING, dateTime);

        int total = 0;
        int successed = 0;
        int failed = 0;
        for(UserMembershipInfo userInfo : withdrawlList) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // 변수 설정
            Map<String, String> variables = new HashMap<>();
            variables.put("serviceName", SERVICE_NAME);
            variables.put("afterHours", AFTER_HOURS);
            variables.put("withdrawDay", userInfo.getDelDate().format(formatter));
            variables.put("cancelMethod", CANCEL_METHOD);

            boolean sendResult = false;
            if(userInfo.getJoinType().equals("BASIC")) {  // 일반 회원가입 : 알림톡/문자 발송
                // 알림톡 발송
                sendResult = alimTalkSendService.sendWithdrawalAlimTalk(encryptUtil.decrypt(userInfo.getTel()), variables);

            } else {  // oAuth 회원가입 : 이메일 발송
                variables.put("supportEmail", "support@fitpl.com");
                variables.put("companyKorName", "핏플");
                variables.put("bizNumber", "000-00-00000");
                variables.put("ceoName", "홍길동");
                variables.put("address", "서울특별시 OO구 OO로 00");
                variables.put("tel", "0000-0000");
                variables.put("companyEngName", "Fitpl");

                SendMailDTO sendMailDTO = new SendMailDTO();
                sendMailDTO.setFrom(DIRECT_SEND_FROM);
                sendMailDTO.setTo(encryptUtil.decrypt(userInfo.getEmail()));
                sendMailDTO.setSubject(SUBJECT);
                sendResult = emailSendService.sendWithdrawlEmail(sendMailDTO, variables);
            }

            if(sendResult) {
                successed++;
            } else {
                failed++;
            }
        }
        log.info("[ WithdrawlBatchService.processBeforeWithdrawlBatch() ] total={}, successed={}, failed={}", total, successed, failed);
    }
}
