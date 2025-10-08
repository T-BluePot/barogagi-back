package com.barogagi.member.login.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "REFRESH_TOKEN",
        indexes = {
                @Index(name = "idx_refresh_user", columnList = "MEMBERSHIP_NO")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_REFRESH_TOKEN_TOKEN", columnNames = "TOKEN")
        }
)
@Getter
@Setter
public class RefreshToken {

    public enum Status { VALID, REVOKED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "MEMBERSHIP_NO", nullable = false)
    private Long membershipNo;

    @Column(name = "DEVICE_ID", length = 100)
    private String deviceId;

    @Column(name = "TOKEN", length = 512, nullable = false, unique = true)
    private String token;

    @Column(name = "STATUS", length = 20, nullable = false)
    private String status; // VALID / REVOKED

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
}