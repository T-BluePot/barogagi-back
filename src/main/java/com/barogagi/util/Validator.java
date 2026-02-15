package com.barogagi.util;

import com.barogagi.schedule.command.service.ScheduleCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.regex.Pattern;

@Service
public class Validator {
    private static final Logger logger = LoggerFactory.getLogger(Validator.class);

    // 금칙어 목록 예시 (확장 가능)
    private static final String[] BLOCKED_WORDS = {"admin", "운영자"};

    private final String API_SECRET_KEY;

    @Autowired
    public Validator(Environment environment) {
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
    }

    // API SECRET KEY 검증
    public boolean apiSecretKeyCheck(String apiSecretKey) {
        return apiSecretKey.equals(API_SECRET_KEY);
    }

    // 아이디 검증
    public boolean isValidId(String userId) {

        if (userId.contains(" ")) return false;

        // 소문자 시작, 소문자+숫자만, 4~16자
        String regex = "^[a-z][a-z0-9]{3,15}$";
        return Pattern.matches(regex, userId);
    }

    // 비밀번호 검증
    public boolean isValidPassword(String password) {
        // 공백 확인
        if (password.contains(" ")) return false;

        // 길이 확인
        if (password.length() < 8 || password.length() > 20) return false;

        // 반복 문자 확인 (예: aaa, 1111)
        if (hasRepeatedChars(password)) return false;

        // 영문, 숫자, 특수문자 각각 포함 여부
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>\\[\\]\\\\/+=_-].*");

        return hasLetter && hasDigit && hasSpecial;
    }

    // 반복 문자 체크 함수
    private boolean hasRepeatedChars(String str) {
        for (int i = 0; i < str.length() - 2; i++) {
            char c1 = str.charAt(i);
            char c2 = str.charAt(i + 1);
            char c3 = str.charAt(i + 2);
            if (c1 == c2 && c2 == c3) {
                return true; // 3글자 이상 반복되면 실패
            }
        }
        return false;
    }

    // 닉네임 검증
    public boolean isValidNickname(String nickname) {
        // 한글, 영문, 숫자만 허용 / 특수문자, 공백 X / 2~12자
        String regex = "^[가-힣a-zA-Z0-9]{2,12}$";
        if (!Pattern.matches(regex, nickname)) return false;

        if (nickname.contains(" ")) return false;

        // 금칙어 포함 여부
        for (String word : BLOCKED_WORDS) {
            if (nickname.toLowerCase().contains(word.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    // Integer 검증
    public boolean isInvalidInteger(Integer value) {
        return value == null || value < 0;
    }
}
