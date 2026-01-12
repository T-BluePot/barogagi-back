package com.barogagi.terms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TermsId implements Serializable {

    @Column(name = "TERMS_NUM")
    private int termsNum;

    @Column(name = "MEMBERSHIP_NO")
    private String membershipNo;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof TermsId)) return false;
        TermsId termsId = (TermsId) object;
        return Objects.equals(termsNum, termsId.termsNum)
                && Objects.equals(membershipNo, termsId.membershipNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(termsNum, membershipNo);
    }
}

