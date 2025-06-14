package com.barogagi.util;

import org.springframework.stereotype.Service;

@Service
public class InputValidate {

    /**
     * 입력값이 null이거나 빈 문자열인지 체크
     * @param input 검사할 문자열
     * @return null이거나 빈 문자열이면 true, 아니면 false
     */
    public boolean isEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }
}
