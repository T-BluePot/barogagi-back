package com.barogagi.schedule.query.vo;

import com.barogagi.tag.dto.TagRegistResDTO;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ScheduleListVO {
    public int scheduleNum;        // 일정 번호 (PK)
    public String scheduleNm;      // 일정명
    public String startDate;       // 시작 날짜
    public String endDate;         // 종료 날짜

    List<TagRegistResDTO> scheduleTagRegistResDTOList;
}
