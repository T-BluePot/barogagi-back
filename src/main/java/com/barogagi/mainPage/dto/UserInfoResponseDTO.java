package com.barogagi.mainPage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoResponseDTO {

    // SCHEDULE
    private int scheduleNum = 0;
    private String scheduleNm = "";
    private String startDate = "";

    // PLAN
    private String planNm = "";
    private int planNum = 0;

    // CATEGORY
    private String categoryNm = "";
}
