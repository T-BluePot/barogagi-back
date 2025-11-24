package com.barogagi.mainPage.mapper;

import com.barogagi.mainPage.dto.RegionInfoDTO;
import com.barogagi.mainPage.dto.TagInfoDTO;
import com.barogagi.mainPage.dto.UserInfoRequestDTO;
import com.barogagi.mainPage.dto.UserInfoResponseDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MainPageMapper {

    // 유저 일정 조회
    UserInfoResponseDTO selectUserScheduleInfo(UserInfoRequestDTO userInfoRequestDTO);

    // 해당 schedule에 대한 태그 목록 조회
    List<TagInfoDTO> selectScheduleTag(UserInfoRequestDTO userInfoRequestDTO);

    // 해당 plan에 대한 region 정보 조회
    RegionInfoDTO selectScheduleRegionInfo(UserInfoRequestDTO userInfoRequestDTO);

    // 인기 지역 조회

    // 인기 태그 조회
}
