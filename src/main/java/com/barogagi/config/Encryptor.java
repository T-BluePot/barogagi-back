package com.barogagi.config;

import org.jasypt.util.text.AES256TextEncryptor;

public class Encryptor {
    public static void main(String[] args) {
        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword("암호화키"); // 본인이 설정한 비밀키
        String encrypted = encryptor.encrypt("mypassword");
        System.out.println("암호화된 값: " + encrypted);
    }
}
