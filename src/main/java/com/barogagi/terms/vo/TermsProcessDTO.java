package com.barogagi.terms.vo;

import com.barogagi.config.vo.DefaultVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TermsProcessDTO {
    private int termsNum = 0;
    private String userId = "";
    private String agreeYn = "";
    private String membershipNo = "";
}
