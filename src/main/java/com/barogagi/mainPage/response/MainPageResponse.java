package com.barogagi.mainPage.response;

import com.barogagi.mainPage.dto.RegionInfoDTO;
import com.barogagi.mainPage.dto.TagInfoDTO;
import com.barogagi.mainPage.dto.UserInfoResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MainPageResponse {
    // 결과 코드
    private String resultCode;

    // 결과 메시지
    private String message;

    // 데이터
    private UserInfoResponseDTO userInfoResponseDTO;
    private List<TagInfoDTO> tagInfoList;
    private RegionInfoDTO regionInfoDTO;
}
