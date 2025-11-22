package com.barogagi.terms.dto;

import com.barogagi.config.vo.DefaultVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TermsInputDTO extends DefaultVO {
    private String termsType = "";
}
