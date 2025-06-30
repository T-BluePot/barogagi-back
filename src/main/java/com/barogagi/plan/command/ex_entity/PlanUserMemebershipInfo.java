package com.barogagi.plan.command.ex_entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Builder(toBuilder = true)
@Table(name = "USER_MEMBERSHIP_INFO")
public class PlanUserMemebershipInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBERSHIP_NO")
    private Integer membershipNo;           // 회원번호

    @Column(name = "USER_ID", nullable = false, length = 100)
    private String userId;                  // 아이디

    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;                   // 이메일

    @Column(name = "PASSWORD", nullable = false, length = 100)
    private String password;                // 비밀번호

    @Column(name = "BIRTH", length = 255)
    private String birth;                   // 생년월일

    @Column(name = "TEL", length = 255)
    private String tel;                     // 전화번호

    @Column(name = "GENDER", length = 255)
    private String gender;                  // 성별

    @Column(name = "REG_DATE", length = 255)
    private String regDate;                 // 등록일

    @Column(name = "UPD_DATE", length = 255)
    private String updDate;                 //수정일
}
