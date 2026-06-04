package com.barogagi.batch.service;

import com.barogagi.batch.dto.ScheduleDTO;
import com.barogagi.batch.mapper.PushBatchMapper;
import com.barogagi.push.service.PushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushBatchService {

    private final PushService pushService;

    private final PushBatchMapper pushBatchMapper;

    // 일정 시작 24시간 전 푸쉬 알림
    public void scheduleStartPushBatch() {

        // 1. 타켓 조회
        List<ScheduleDTO> targets = pushBatchMapper.selectSchedulePushTarget();

        // 푸쉬 일괄 발송
        for(ScheduleDTO scheduleDTO : targets) {

            String title = "내일 예정된 일정이 있습니다";
            String body = String.format("[%s] 일정이 하루 앞으로 다가왔습니다.", scheduleDTO.getScheduleNm());

            pushService.sendToUser(scheduleDTO.getMembershipNo(), title, body);
        }
    }
}
