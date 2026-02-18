package com.barogagi.terms.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TERMS_AGREE")
@Getter
@Setter
public class TermsAgree {

    @EmbeddedId
    private TermsId id;

    @Schema(description = "동의 여부", example = "Y:동의 / N:비동의")
    @Enumerated(EnumType.STRING)
    @Column(name = "AGREE_YN", nullable = false)
    private AgreeYn agreeYn;
}
