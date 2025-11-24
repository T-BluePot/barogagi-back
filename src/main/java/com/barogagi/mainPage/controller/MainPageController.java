package com.barogagi.mainPage.controller;

import com.barogagi.mainPage.dto.RegionInfoDTO;
import com.barogagi.mainPage.dto.TagInfoDTO;
import com.barogagi.mainPage.dto.UserInfoRequestDTO;
import com.barogagi.mainPage.dto.UserInfoResponseDTO;
import com.barogagi.mainPage.exception.MainPageException;
import com.barogagi.mainPage.response.MainPageResponse;
import com.barogagi.mainPage.service.MainPageService;
import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "메인 화면", description = "메인 화면에 필요한 API")
@RestController
@RequestMapping("/main/page")
public class MainPageController {

    private static final Logger logger = LoggerFactory.getLogger(MainPageController.class);

    private final MainPageService mainPageService;

    @Autowired
    public MainPageController(MainPageService mainPageService) {
        this.mainPageService = mainPageService;
    }

    @Operation(summary = "유저 일정 정보 API", description = "메인 화면 - 다가오는 일정 부분에 해당하는 API")
    @PostMapping("/user/schedule/info")
    public MainPageResponse selectUserScheduleInfo(HttpServletRequest request) {

        logger.info("CALL /main/page/user/schedule/info");

        MainPageResponse mainPageResponse = new MainPageResponse();
        String resultCode = "";
        String message = "";

        try {

            // 회원번호
            String membershipNo = String.valueOf(request.getAttribute("membershipNo"));

            logger.info("@@ membershipNo.isEmpty()={}", membershipNo.isEmpty());
            if (membershipNo.isEmpty()) {
                throw new MainPageException("401", "접근 권한이 존재하지 않습니다.");
            }

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
}
