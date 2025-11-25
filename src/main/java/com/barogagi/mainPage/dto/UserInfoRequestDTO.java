package com.barogagi.mainPage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoRequestDTO {

    private String membershipNo = "";

    // SCHEDULE
    private int scheduleNum = 0;

    // PLAN
    private int planNum = 0;
}
