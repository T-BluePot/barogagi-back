package com.barogagi.terms.dto;

import com.barogagi.terms.domain.AgreeYn;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TermsAgreeDTO {
    private String membershipNo = "";
    private int termsNum = 0;

    @Enumerated(EnumType.STRING)
    private AgreeYn agreeYn;
}
