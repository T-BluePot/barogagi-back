package com.barogagi.mainPage.service;

import com.barogagi.mainPage.dto.RegionInfoDTO;
import com.barogagi.mainPage.dto.TagInfoDTO;
import com.barogagi.mainPage.dto.UserInfoRequestDTO;
import com.barogagi.mainPage.dto.UserInfoResponseDTO;
import com.barogagi.mainPage.mapper.MainPageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MainPageService {

    private static final Logger logger = LoggerFactory.getLogger(MainPageService.class);

    private final MainPageMapper mainPageMapper;

    @Autowired
    public MainPageService(MainPageMapper mainPageMapper) {
        this.mainPageMapper = mainPageMapper;
    }

    // 유저 일정 조회
    public UserInfoResponseDTO selectUserScheduleInfo(UserInfoRequestDTO userInfoRequestDTO) throws Exception {
        return mainPageMapper.selectUserScheduleInfo(userInfoRequestDTO);
    }

    // 해당 schedule에 대한 태그 목록 조회
    public List<TagInfoDTO> selectScheduleTag(UserInfoRequestDTO userInfoRequestDTO) throws Exception {
        return  mainPageMapper.selectScheduleTag(userInfoRequestDTO);
    }

    // 해당 plan에 대한 region 정보 조회
    public RegionInfoDTO selectScheduleRegionInfo(UserInfoRequestDTO userInfoRequestDTO) throws Exception {
        return mainPageMapper.selectScheduleRegionInfo(userInfoRequestDTO);
    }

    // 인기 지역 조회

    // 인기 태그 조회
}
