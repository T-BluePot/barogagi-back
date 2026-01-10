package com.barogagi.ai.dto;
import lombok.*;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
public class AIReqWrapper {
    private List<String> tags;
    private String comment;
    private List<AIReqDTO> placeList;
}