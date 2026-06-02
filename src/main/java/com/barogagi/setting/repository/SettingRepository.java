package com.barogagi.setting.repository;

import com.barogagi.setting.entity.Setting;
import com.barogagi.setting.enums.SettingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Integer> {

    @Query("""
            SELECT s
            FROM Setting s
            WHERE s.settingType = :settingType
            AND s.useYn = 'Y'
            """)
    Setting selectSetting(@Param("settingType") SettingType settingType);
}
