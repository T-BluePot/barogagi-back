package com.barogagi.member.withdraw.service;

import com.barogagi.member.repository.WithdrawReasonCodeRepository;
import com.barogagi.member.withdraw.domain.WithdrawReasonCode;
import com.barogagi.member.withdraw.exception.WithdrawException;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WithdrawalReasonService {

    private final Validator validator;

    private final WithdrawReasonCodeRepository withdrawReasonCodeRepository;

    public ApiResponse getWithdrawalReasons(String apiSecretKey) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new WithdrawException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 탈퇴 사유 조회
        List<WithdrawReasonCode> withdrawReasonCode = withdrawReasonCodeRepository.findWithdrawReasonCode("Y");

        if(withdrawReasonCode.isEmpty()) {
            throw new WithdrawException(ErrorCode.FAIL_FIND_WITHDRAW_CODE);

        } else {
            return ApiResponse.resultData(withdrawReasonCode,
                    ErrorCode.SUCCESS_FIND_WITHDRAW_CODE.getCode(),
                    ErrorCode.SUCCESS_FIND_WITHDRAW_CODE.getMessage());
        }
    }
}
