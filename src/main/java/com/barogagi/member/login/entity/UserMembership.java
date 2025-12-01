package com.barogagi.member.login.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "USER_MEMBERSHIP_INFO")
@Getter
@Setter
public class UserMembership {

    @Id
    @Column(name = "MEMBERSHIP_NO", nullable = false, length = 100)
    private String membershipNo;

    @Column(name = "USER_ID", nullable = false, length = 100)
    private String userId;

    @Column(name = "PASSWORD", length = 100)
    private String password;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "BIRTH", length = 100)
    private String birth;

    @Column(name = "TEL", length = 100)
    private String tel;

    @Column(name = "GENDER", length = 1)
    private String gender; // M / W

    @Column(name = "NICKNAME", length = 100)
    private String nickname;

    @Column(name = "REG_DATE")
    private LocalDateTime regDate;

    @Column(name = "UPD_DATE")
    private LocalDateTime updDate;

    @Column(name = "JOIN_TYPE", nullable = false, length = 100)
    private String joinType; // BASIC / GOOGLE / NAVER / KAKAO
}
