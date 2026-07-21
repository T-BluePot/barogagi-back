package com.barogagi.mainPage.service;

import com.barogagi.batch.entity.KorTourOrgLocalCode;
import com.barogagi.batch.entity.LocalPopularReplace;
import com.barogagi.batch.repository.KorTourOrgLocalCodeRepository;
import com.barogagi.batch.repository.LocalPopularReplaceRepository;
import com.barogagi.mainPage.dto.*;
import com.barogagi.mainPage.exception.MainPageException;
import com.barogagi.mainPage.mapper.MainPageMapper;
import com.barogagi.mainPage.response.MainPageResponse;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.MembershipUtil;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainPageService {

    private final KorTourOrgLocalCodeRepository korTourOrgLocalCodeRepository;
    private final LocalPopularReplaceRepository localPopularReplaceRepository;
    private final MainPageMapper mainPageMapper;
    private final MembershipUtil membershipUtil;
    private final Validator validator;


    public MainPageResponse selectUserScheduleInfoProcess(HttpServletRequest request) {

        String resultCode = "";
        String message = "";
        List<TagInfoDTO> tagList = null;
        RegionInfoDTO regionInfo = null;
        UserInfoResponseDTO userInfoResponseDTO = null;

        // 1. 회원번호 구하기
        Map<String, Object> membershipNoInfo = membershipUtil.membershipNoService(request);
        if(!membershipNoInfo.get("resultCode").equals("A200")) {

            return MainPageResponse.resultData(
                    userInfoResponseDTO, tagList, regionInfo,
                    String.valueOf(membershipNoInfo.get("resultCode")),
                    String.valueOf(membershipNoInfo.get("message"))
            );
        }

        String membershipNo = String.valueOf(membershipNoInfo.get("membershipNo"));

        // 2. 유저 일정 정보 조회
        UserInfoRequestDTO userInfoRequestDTO = new UserInfoRequestDTO();
        userInfoRequestDTO.setMembershipNo(membershipNo);
        userInfoResponseDTO = this.selectUserScheduleInfo(userInfoRequestDTO);

        if(null == userInfoResponseDTO) {
            resultCode = ErrorCode.NOT_FOUND_SCHEDULE.getCode();
            message = ErrorCode.NOT_FOUND_SCHEDULE.getMessage();
        } else {

            resultCode = ErrorCode.FOUND_SCHEDULE.getCode();
            message = ErrorCode.FOUND_SCHEDULE.getMessage();

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
            throw new MainPageException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 인기 태그 조회
        List<TagRankInfoDTO> tagRankInfoList = this.selectTagRankList();
        if(tagRankInfoList.isEmpty()) {
            throw new MainPageException(ErrorCode.NOT_FOUND_POPULAR_TAG);

        }

        return ApiResponse.resultData(
                tagRankInfoList,
                ErrorCode.FOUND_POPULAR_TAG.getCode(),
                ErrorCode.FOUND_POPULAR_TAG.getMessage()
        );
    }

    public ApiResponse selectPopularRegionList(String apiSecretKey) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new MainPageException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 인기 지역 조회
        List<RegionRankInfoDTO> regionRankInfoList = this.selectRegionRankList();

        if(regionRankInfoList.isEmpty()) {
            throw new MainPageException(ErrorCode.NOT_FOUND_POPULAR_REGION);
        }

        return ApiResponse.resultData(
                regionRankInfoList,
                ErrorCode.FOUND_POPULAR_REGION.getCode(),
                ErrorCode.FOUND_POPULAR_REGION.getMessage()
        );
    }

    public ApiResponse selectKorTourOrgLocalCode(String apiSecretKey,
                                               String type) {
        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new MainPageException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 지역 코드 조회
        List<KorTourOrgLocalCode> localCodeList = null;
        if(type.equals("HOT-PLACE")) {
            localCodeList = korTourOrgLocalCodeRepository.findLocalCode("areaBasedList1");
        }

        if(localCodeList == null) {
            throw new MainPageException(ErrorCode.NOT_FOUND_LOCAL_CODE);
        }

        return ApiResponse.resultData(localCodeList, "C200", "지역 코드 정보를 조회하였습니다.");
    }

    public ApiResponse selectHotPlaceList(String apiSecretKey, String areaCd, String sigunguCd) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new MainPageException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        if(null == areaCd || null == sigunguCd) {
            areaCd = "11";
            sigunguCd = "11110";
        }

        // 2. 지역별 인기 장소 조회
        List<LocalPopularReplace> findLocalPopularReplace = localPopularReplaceRepository.findLocalPopularReplace(areaCd, sigunguCd);

        if(findLocalPopularReplace.isEmpty()) {
            throw new MainPageException(ErrorCode.NOT_FOUND_HOT_PLACE);
        }

        return ApiResponse.resultData(findLocalPopularReplace, "P200", "해당 지역의 인기 장소를 조회하였습니다.");
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
