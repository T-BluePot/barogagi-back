package com.barogagi.terms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TermsOutputDTO {
    private int termsNum = 0;
    private String title = "";
    private String contents = "";
    private String termsType = "";
    private String useYn = "";
    private String regDate = "";
    private String essentialYn = "";
    private int sort = 0;
}
