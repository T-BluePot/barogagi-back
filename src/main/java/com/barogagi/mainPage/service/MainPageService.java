package com.barogagi.mainPage.service;

import com.barogagi.mainPage.dto.*;
import com.barogagi.mainPage.exception.MainPageException;
import com.barogagi.mainPage.mapper.MainPageMapper;
import com.barogagi.mainPage.response.MainPageResponse;
import com.barogagi.config.resultCode.ProcessResultCode;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.MembershipUtil;
import com.barogagi.config.resultCode.ResultCode;
import com.barogagi.util.Validator;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MainPageService {

    private static final Logger logger = LoggerFactory.getLogger(MainPageService.class);

    private final MainPageMapper mainPageMapper;
    private final MembershipUtil membershipUtil;
    private final Validator validator;

    @Autowired
    public MainPageService(
                            MainPageMapper mainPageMapper,
                            MembershipUtil membershipUtil,
                            Validator validator
                            )
    {
        this.mainPageMapper = mainPageMapper;
        this.membershipUtil = membershipUtil;
        this.validator = validator;
    }

    public MainPageResponse selectUserScheduleInfoProcess(HttpServletRequest request) {

        String resultCode = "";
        String message = "";
        List<TagInfoDTO> tagList = null;
        RegionInfoDTO regionInfo = null;

        // 1. 회원번호 구하기
        Map<String, Object> membershipNoInfo = membershipUtil.membershipNoService(request);
        if(!membershipNoInfo.get("resultCode").equals("200")) {
            throw new MainPageException(
                    String.valueOf(membershipNoInfo.get("resultCode")),
                    String.valueOf(membershipNoInfo.get("message"))
            );
        }

        String membershipNo = String.valueOf(membershipNoInfo.get("membershipNo"));

        // 2. 유저 일정 정보 조회
        UserInfoRequestDTO userInfoRequestDTO = new UserInfoRequestDTO();
        userInfoRequestDTO.setMembershipNo(membershipNo);
        UserInfoResponseDTO userInfoResponseDTO = this.selectUserScheduleInfo(userInfoRequestDTO);

        if(null == userInfoResponseDTO) {
            resultCode = ProcessResultCode.NOT_FOUND_SCHEDULE.getResultCode();
            message = ProcessResultCode.NOT_FOUND_SCHEDULE.getMessage();
        } else {

            resultCode = ProcessResultCode.FOUND_SCHEDULE.getResultCode();
            message = ProcessResultCode.FOUND_SCHEDULE.getMessage();

            // 3. 해당 schedule에 대한 태그 목록 조회
            userInfoRequestDTO.setScheduleNum(userInfoResponseDTO.getScheduleNum());
            tagList = this.selectScheduleTag(userInfoRequestDTO);

            // 4. 해당 plan에 대한 region 정보 조회
            userInfoRequestDTO.setPlanNum(userInfoResponseDTO.getPlanNum());
            regionInfo = this.selectScheduleRegionInfo(userInfoRequestDTO);
        }

        return MainPageResponse.resultData(userInfoResponseDTO, tagList, regionInfo, resultCode, message);
    }

    public ApiResponse selectPopularTagList(String apiSecretKey) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new MainPageException(
                    ResultCode.NOT_EQUAL_API_SECRET_KEY.getResultCode(),
                    ResultCode.NOT_EQUAL_API_SECRET_KEY.getMessage()
            );
        }

        // 2. 인기 태그 조회
        List<TagRankInfoDTO> tagRankInfoList = this.selectTagRankList();
        if(tagRankInfoList.isEmpty()) {
            throw new MainPageException(
                    ProcessResultCode.NOT_FOUND_POPULAR_TAG.getResultCode(),
                    ProcessResultCode.NOT_FOUND_POPULAR_TAG.getMessage()
            );

        }

        return ApiResponse.resultData(
                tagRankInfoList,
                ProcessResultCode.FOUND_POPULAR_TAG.getResultCode(),
                ProcessResultCode.FOUND_POPULAR_TAG.getMessage()
        );
    }

    public ApiResponse selectPopularRegionList(String apiSecretKey) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new MainPageException(
                    ResultCode.NOT_EQUAL_API_SECRET_KEY.getResultCode(),
                    ResultCode.NOT_EQUAL_API_SECRET_KEY.getMessage()
            );
        }

        // 2. 인기 지역 조회
        List<RegionRankInfoDTO> regionRankInfoList = this.selectRegionRankList();

        if(regionRankInfoList.isEmpty()) {
            throw new MainPageException(
                    ProcessResultCode.NOT_FOUND_POPULAR_REGION.getResultCode(),
                    ProcessResultCode.NOT_FOUND_POPULAR_REGION.getMessage()
            );
        }

        return ApiResponse.resultData(
                regionRankInfoList,
                ProcessResultCode.FOUND_POPULAR_REGION.getResultCode(),
                ProcessResultCode.FOUND_POPULAR_REGION.getMessage()
        );
    }

    // 유저 일정 조회
    public UserInfoResponseDTO selectUserScheduleInfo(UserInfoRequestDTO userInfoRequestDTO) {
        return mainPageMapper.selectUserScheduleInfo(userInfoRequestDTO);
    }

    // 해당 schedule에 대한 태그 목록 조회
    public List<TagInfoDTO> selectScheduleTag(UserInfoRequestDTO userInfoRequestDTO) {
        return  mainPageMapper.selectScheduleTag(userInfoRequestDTO);
    }

    // 해당 plan에 대한 region 정보 조회
    public RegionInfoDTO selectScheduleRegionInfo(UserInfoRequestDTO userInfoRequestDTO) {
        return mainPageMapper.selectScheduleRegionInfo(userInfoRequestDTO);
    }

    // 인기 지역 조회
    public List<RegionRankInfoDTO> selectRegionRankList() {
        return mainPageMapper.selectRegionRankList();
    }

    // 인기 태그 조회
    public List<TagRankInfoDTO> selectTagRankList() {
        return mainPageMapper.selectTagRankList();
    }
}
