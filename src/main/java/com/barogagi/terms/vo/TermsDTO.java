package com.barogagi.terms.vo;

import com.barogagi.config.vo.DefaultVO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TermsDTO extends DefaultVO {
    private int termsNum = 0;
    private String userId = "";
    private String agreeYn = "";

    @ArraySchema(schema = @Schema(implementation = TermsProcessDTO.class))
    private List<TermsProcessDTO> termsAgreeList;
}
