package com.barogagi.setting.mapper;

import com.barogagi.setting.dto.MemberSettingDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SettingMapper {

    // 설정 목록 조회 (회원 상세페이지에서 조회)
    List<MemberSettingDTO> selectSettings(String membershipNo);
}
