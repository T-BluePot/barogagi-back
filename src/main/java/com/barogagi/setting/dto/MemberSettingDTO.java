package com.barogagi.setting.dto;

import com.barogagi.setting.enums.SettingType;
import com.barogagi.setting.enums.Value;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSettingDTO {

    private int id;

    @Enumerated(EnumType.STRING)
    private SettingType settingType;

    private String settingContent;

    private int sort;

    @Enumerated(EnumType.STRING)
    private Value value;

    private String beforeValue;
}
