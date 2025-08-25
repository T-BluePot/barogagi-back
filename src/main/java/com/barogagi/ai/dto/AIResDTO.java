package com.barogagi.ai.dto;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
public class AIResDTO {
//    private String requestId;
//    private String model;
//    private String content;
    private Integer recommandPlaceNum;
    private String aiDescription;
}
