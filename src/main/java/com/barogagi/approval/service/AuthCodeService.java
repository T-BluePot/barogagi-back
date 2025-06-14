package com.barogagi.approval.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AuthCodeService {

    public String generateAuthCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);  // 100000 ~ 999999
        return String.valueOf(code);
    }
}
