package com.barogagi.terms.dto;


import com.barogagi.terms.domain.AgreeYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TermsProcessDTO {
    private int termsNum = 0;
    private AgreeYn agreeYn;
}
