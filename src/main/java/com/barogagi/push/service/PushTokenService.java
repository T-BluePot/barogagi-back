package com.barogagi.push.service;

import com.barogagi.push.entity.PushToken;
import com.barogagi.push.dto.PushTokenRequest;
import com.barogagi.push.repository.PushTokenRepository;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.InputValidate;
import com.barogagi.util.MembershipUtil;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PushTokenService {

    private final PushTokenRepository repository;
    private final MembershipUtil membershipUtil;
    private final InputValidate inputValidate;

    public ApiResponse saveToken(HttpServletRequest request, PushTokenRequest pushTokenRequest) {

        // 1. 회원번호 구하기
        Map<String, Object> membershipNoInfo = membershipUtil.membershipNoService(request);
        if(!membershipNoInfo.get("resultCode").equals("A200")) {
            return ApiResponse.error(
                    ErrorCode.NOT_FOUND_USER_INFO.getCode(),
                    ErrorCode.NOT_FOUND_USER_INFO.getMessage()
            );
        }
        String membershipNo = String.valueOf(membershipNoInfo.get("membershipNo"));

        Optional<PushToken> optional = repository.findByMembershipNoAndDeviceTypeAndActiveYn(membershipNo, pushTokenRequest.getDeviceType(), "Y");

        PushToken entity;

        if (optional.isPresent()) {  // 이미 존재하는 token
            entity = optional.get();
            entity.setActiveYn("Y");
            entity.setDeviceType(pushTokenRequest.getDeviceType());
            entity.setAppVersion(pushTokenRequest.getAppVersion());
            entity.setFcmToken(pushTokenRequest.getFcmToken());

        } else {  // 신규 token 저장
            entity = new PushToken();
            entity.setMembershipNo(membershipNo);
            entity.setFcmToken(pushTokenRequest.getFcmToken());
            entity.setDeviceType(pushTokenRequest.getDeviceType());
            entity.setAppVersion(pushTokenRequest.getAppVersion());
            entity.setActiveYn("Y");
            repository.save(entity);
        }

        return ApiResponse.result("T200", "FCM TOKEN 저장이 완료되었습니다.");
    }

    public ApiResponse delToken(HttpServletRequest request, String fcmToken, String deviceType) {

        // 1. 회원번호 구하기
        Map<String, Object> membershipNoInfo = membershipUtil.membershipNoService(request);
        if(!membershipNoInfo.get("resultCode").equals("A200")) {
            return ApiResponse.error(
                    ErrorCode.NOT_FOUND_USER_INFO.getCode(),
                    ErrorCode.NOT_FOUND_USER_INFO.getMessage()
            );
        }
        String membershipNo = String.valueOf(membershipNoInfo.get("membershipNo"));

        // 2. 요청 파라미터 체크
        boolean deleteAll = false;
        if(inputValidate.isEmpty(deviceType) || inputValidate.isEmpty(fcmToken)) {
            deleteAll = true;
        }

        // 3. 토큰 조회
        if(deleteAll) {
            repository.deleteByMembershipNoAndActiveYn(membershipNo, "Y");

        } else {
            repository.deleteByMembershipNoAndFcmTokenAndDeviceTypeAndActiveYn(membershipNo, fcmToken, deviceType, "Y");
        }

        return ApiResponse.result("TD200", "FCM TOKEN 삭제가 완료되었습니다.");
    }
}
