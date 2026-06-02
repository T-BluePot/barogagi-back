package com.barogagi.setting.entity;

import com.barogagi.setting.enums.SettingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SETTING")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Setting {

    @Id
    @Column(name = "ID", nullable = false)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "SETTING_TYPE", nullable = false)
    private SettingType settingType;

    @Column(name = "SETTING_CONTENT", nullable = false)
    private String settingContent;

    @Column(name = "SORT", nullable = false)
    private int sort;

    @Column(name = "USE_YN", nullable = false)
    private String useYn;
}
