package com.barogagi.batch.service;

import com.barogagi.batch.dto.ScheduleDTO;
import com.barogagi.batch.mapper.PushBatchMapper;
import com.barogagi.push.service.PushService;
import com.barogagi.setting.dto.MemberSettingDTO;
import com.barogagi.setting.enums.SettingType;
import com.barogagi.setting.enums.Value;
import com.barogagi.setting.service.SettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushBatchService {

    private final PushService pushService;
    private final SettingService settingService;

    private final PushBatchMapper pushBatchMapper;

    // 일정 시작 24시간 전 푸쉬 알림
    public void scheduleStartPushBatch() {

        // 1. 타켓 조회
        List<ScheduleDTO> targets = pushBatchMapper.selectSchedulePushTarget();

        // 푸쉬 일괄 발송
        for(ScheduleDTO scheduleDTO : targets) {

            // 알림 ON 여부
            MemberSettingDTO memberSettingDTO = new MemberSettingDTO();
            memberSettingDTO.setSettingType(SettingType.PUSH_NOTIFICATION);
            memberSettingDTO.setMembershipNo(scheduleDTO.getMembershipNo());

            MemberSettingDTO settingDTO = settingService.selectMemberSettingValue(memberSettingDTO);

            log.info("setting value={}", settingDTO.getValue());
            if(settingDTO.getValue() == Value.ON) {

                String title = "내일 예정된 일정이 있습니다";
                String body = String.format("[%s] 일정이 하루 앞으로 다가왔습니다.", scheduleDTO.getScheduleNm());

                pushService.sendToUser(settingDTO.getMembershipNo(), title, body);
            }
        }
    }
}
