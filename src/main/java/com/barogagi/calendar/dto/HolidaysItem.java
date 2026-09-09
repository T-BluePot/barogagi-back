package com.barogagi.calendar.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HolidaysItem {

    private String dateKind;
    private String dateName;
    private String isHoliday;
    private int locdate;
    private int seq;
}