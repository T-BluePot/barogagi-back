package com.barogagi.terms.vo;

import com.barogagi.config.vo.DefailtVO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TermsVO extends DefailtVO {
    private int termsNum = 0;
    private String title = "";
    private String contents = "";
    private String termsType = "";
    private String useYn = "";
    private String regDate = "";
    private String essentialYn = "";
    private int sort = 0;

    private String userId = "";
    private String membershipNo = "";

    private String agreeYn = "";

    @ArraySchema(schema = @Schema(implementation = TermsVO.class))
    private List<TermsVO> termsAgreeList;
}
