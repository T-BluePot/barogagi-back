package com.barogagi.mainPage.controller;

import com.barogagi.config.vo.DefaultVO;
import com.barogagi.mainPage.dto.*;
import com.barogagi.mainPage.exception.MainPageException;
import com.barogagi.mainPage.response.MainPageResponse;
import com.barogagi.mainPage.service.MainPageService;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.MembershipUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "메인 화면", description = "메인 화면에 필요한 API")
@RestController
@RequestMapping("/main/page")
public class MainPageController {

    private static final Logger logger = LoggerFactory.getLogger(MainPageController.class);

    private final MainPageService mainPageService;

    private final MembershipUtil membershipUtil;

    private final String API_SECRET_KEY;

    @Autowired
    public MainPageController(MainPageService mainPageService,
                              MembershipUtil membershipUtil,
                              Environment environment) {
        this.mainPageService = mainPageService;
        this.membershipUtil = membershipUtil;
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
    }

    @Operation(summary = "유저 일정 정보 API", description = "메인 화면 - 다가오는 일정 부분에 해당하는 API",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "접근 권한이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "일정이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/user/schedule/info")
    public MainPageResponse selectUserScheduleInfo(HttpServletRequest request) {

        logger.info("CALL /main/page/user/schedule/info");

        MainPageResponse mainPageResponse = new MainPageResponse();
        String resultCode = "";
        String message = "";

        try {

            // 회원번호 구하기
            Map<String, Object> membershipNoInfo = membershipUtil.MembershipNoService(request);
            if(!membershipNoInfo.get("resultCode").equals("200")) {
                throw new MainPageException(String.valueOf(membershipNoInfo.get("resultCode")),
                                            String.valueOf(membershipNoInfo.get("message")));
            }

            String membershipNo = String.valueOf(membershipNoInfo.get("membershipNo"));

            // 유저 일정 정보 API
            UserInfoRequestDTO userInfoRequestDTO = new UserInfoRequestDTO();
            userInfoRequestDTO.setMembershipNo(membershipNo);

            UserInfoResponseDTO userInfoResponseDTO = mainPageService.selectUserScheduleInfo(userInfoRequestDTO);

            if(null == userInfoResponseDTO) {
                resultCode = "201";
                message = "일정이 존재하지 않습니다.";

            } else {

                resultCode = "200";
                message = "조회 성공하였습니다.";

                mainPageResponse.setUserInfoResponseDTO(userInfoResponseDTO);

                // 일정 번호
                userInfoRequestDTO.setScheduleNum(userInfoResponseDTO.getScheduleNum());

                // 해당 schedule에 대한 태그 목록 조회
                List<TagInfoDTO> tagList = mainPageService.selectScheduleTag(userInfoRequestDTO);

                if(!tagList.isEmpty()) {
                    mainPageResponse.setTagInfoList(tagList);
                }

                // 해당 plan에 대한 region 정보 조회
                userInfoRequestDTO.setPlanNum(userInfoResponseDTO.getPlanNum());

                RegionInfoDTO regionInfo = mainPageService.selectScheduleRegionInfo(userInfoRequestDTO);

                if(null != regionInfo) {
                    mainPageResponse.setRegionInfoDTO(regionInfo);
                }
            }

        } catch (MainPageException ex) {
            resultCode = ex.getResultCode();
            message = ex.getMessage();

        } catch (Exception e) {
            logger.error("error", e);
            resultCode = "400";
            message = "오류가 발생하였습니다.";
        } finally {
            mainPageResponse.setResultCode(resultCode);
            mainPageResponse.setMessage(message);
        }

        return mainPageResponse;
    }

    @Operation(summary = "인기 태그 조회 API ", description = "메인 화면 - 오늘 많이 생성되는 일정 부분에 해당하는 API",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인기 태그 조회 완료하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "인기 태그 목록이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/popular/tag/list")
    public ApiResponse selectPopularTagList(@RequestBody DefaultVO vo) {

        logger.info("CALL /main/page/popular/tag/list");

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            if(!vo.getApiSecretKey().equals(API_SECRET_KEY)) {
                throw new MainPageException("100", "잘못된 접근입니다.");
            }

            // 인기 태그 조회
            List<TagRankInfoDTO> tagRankInfoList = mainPageService.selectTagRankList();

            logger.info("@@ !tagRankInfoList.isEmpty()={}", !tagRankInfoList.isEmpty());
            if(!tagRankInfoList.isEmpty()) {
                resultCode = "200";
                message = "인기 태그 조회 완료하였습니다.";
                apiResponse.setData(tagRankInfoList);
            } else {
                resultCode = "201";
                message = "인기 태그 목록이 존재하지 않습니다.";
            }

        } catch (MainPageException ex) {
            resultCode = ex.getResultCode();
            message = ex.getMessage();

        } catch (Exception e) {
            logger.error("error", e);
            resultCode = "400";
            message = "오류가 발생하였습니다.";
        } finally {
            apiResponse.setResultCode(resultCode);
            apiResponse.setMessage(message);
        }

        return apiResponse;
    }

    @Operation(summary = "인기 지역 조회 API ", description = "메인 화면 - 지금 인기많은 핫 플레이스 부분에 해당하는 API",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인기 지역 조회 완료하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "인기 지역 목록이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/popular/region/list")
    public ApiResponse selectPopularRegionList(@RequestBody DefaultVO vo) {

        logger.info("CALL /main/page/popular/region/list");

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            if(!vo.getApiSecretKey().equals(API_SECRET_KEY)) {
                throw new MainPageException("100", "잘못된 접근입니다.");
            }

            // 인기 지역 조회
            List<RegionRankInfoDTO> regionRankInfoList = mainPageService.selectRegionRankList();

            logger.info("@@ !regionRankInfoList.isEmpty()={}", !regionRankInfoList.isEmpty());
            if(!regionRankInfoList.isEmpty()) {
                resultCode = "200";
                message = "인기 지역 조회 완료하였습니다.";
                apiResponse.setData(regionRankInfoList);
            } else {
                resultCode = "201";
                message = "인기 지역 목록이 존재하지 않습니다.";
            }

        } catch (MainPageException ex) {
            resultCode = ex.getResultCode();
            message = ex.getMessage();

        } catch (Exception e) {
            logger.error("error", e);
            resultCode = "400";
            message = "오류가 발생하였습니다.";
        } finally {
            apiResponse.setResultCode(resultCode);
            apiResponse.setMessage(message);
        }

        return apiResponse;
    }
}
