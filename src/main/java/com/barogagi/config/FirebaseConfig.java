package com.barogagi.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {

            InputStream inputStream =
                    getClass().getClassLoader()
                            .getResourceAsStream("firebase/firebase-service-account.json");

            if (inputStream == null) {
                throw new RuntimeException("Firebase json 파일을 찾을 수 없습니다.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully");
            }

        } catch (Exception e) {
            log.error("Firebase initialization failed", e);
            throw new RuntimeException("Firebase 초기화 실패", e);
        }
    }
}