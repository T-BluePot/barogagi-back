package com.barogagi.setting.repository;

import com.barogagi.setting.entity.MemberSetting;
import com.barogagi.setting.entity.MemberSettingId;
import com.barogagi.setting.enums.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberSettingRepository extends JpaRepository<MemberSetting, MemberSettingId> {

    @Modifying
    @Query("""
            UPDATE MemberSetting ms
            SET ms.value = :value
            WHERE ms.membershipNo = :membershipNo
            AND ms.id = :id
            """)
    int updateSetting(
            @Param("membershipNo") String membershipNo,
            @Param("id") int id,
            @Param("value")Value value
    );
}
