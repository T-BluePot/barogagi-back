package com.barogagi.setting.entity;

import com.barogagi.setting.enums.Value;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "MEMBER_SETTING")
@IdClass(MemberSettingId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberSetting {

    @Id
    @Column(name = "ID", nullable = false)
    private int id;

    @Id
    @Column(name = "MEMBERSHIP_NO", nullable = false)
    private String membershipNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "VALUE", nullable = false)
    private Value value;

    @Column(name = "DATE")
    private LocalDateTime date;
}
