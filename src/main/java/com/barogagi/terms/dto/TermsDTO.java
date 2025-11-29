package com.barogagi.terms.dto;

import com.barogagi.config.vo.DefaultVO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TermsDTO extends DefaultVO {
    private String userId = "";

    @ArraySchema(schema = @Schema(implementation = TermsProcessDTO.class))
    private List<TermsProcessDTO> termsAgreeList = new ArrayList<>();
}
