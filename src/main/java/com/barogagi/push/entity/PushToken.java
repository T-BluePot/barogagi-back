package com.barogagi.push.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "PUSH_TOKEN",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"MEMBERSHIP_NO", "FCM_TOKEN"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PushToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 식별값
     */
    @Column(name = "MEMBERSHIP_NO", nullable = false)
    private String membershipNo;

    /**
     * FCM Token
     */
    @Column(name = "FCM_TOKEN" , nullable = false, length = 500)
    private String fcmToken;

    /**
     * ANDROID / IOS / WEB
     */
    @Column(name = "DEVICE_TYPE", length = 20)
    private String deviceType;

    /**
     * 사용 여부
     */
    @Column(name = "ACTIVE_YN", length = 1)
    private String activeYn = "Y";

    /**
     * 앱 버전
     */
    @Column(name = "APP_VERSION", length = 30)
    private String appVersion;

    /**
     * 등록 시간
     */
    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.activeYn = "N";
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.activeYn = "Y";
    }
}
