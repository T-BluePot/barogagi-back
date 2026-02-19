package com.barogagi.member.join.basic.service;

import com.barogagi.member.join.basic.exception.JoinException;
import com.barogagi.member.login.dto.RefreshTokenRequestDTO;
import com.barogagi.member.login.service.AccountService;
import com.barogagi.member.login.service.AuthService;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.InputValidate;
import com.barogagi.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberAccountService {

    private final InputValidate inputValidate;
    private final AuthService authService;
    private final AccountService accountService;

    public ApiResponse withdrawMember(String refreshToken) {

        // 1. refresh token이 공백 또는 null인지 확인
        if(inputValidate.isEmpty(refreshToken)) {
            throw new JoinException(ErrorCode.EMPTY_DATA);
        }

        RefreshTokenRequestDTO refreshTokenRequestDTO = new RefreshTokenRequestDTO();
        refreshTokenRequestDTO.setRefreshToken(refreshToken);

        // 2. refresh token을 이용해서 membershipNo 구하기
        Map<String, String> resultMap = authService.selectUserInfoByToken(refreshTokenRequestDTO.getRefreshToken());
        if(!resultMap.get("resultCode").equals("200")) {
            return ApiResponse.error(
                    resultMap.get("resultCode"),
                    resultMap.get("message")
            );
        }

        int deleteResult = accountService.deleteMyAccount(resultMap.get("membershipNo"));
        if(deleteResult != 1) {
            throw new JoinException(ErrorCode.FAIL_DELETE_ACCOUNT);
        }

        return ApiResponse.result(ErrorCode.SUCCESS_DELETE_ACCOUNT);
    }
}
