package com.barogagi.terms.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TermsDTO {
    private String userId = "";

    @ArraySchema(schema = @Schema(implementation = TermsProcessDTO.class))
    private List<TermsProcessDTO> termsAgreeList = new ArrayList<>();
}
