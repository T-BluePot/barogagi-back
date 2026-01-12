package com.barogagi.terms.repository.spec;

import com.barogagi.terms.domain.Terms;
import org.springframework.data.jpa.domain.Specification;

public class TermsSpec {

    public static Specification<Terms> useYnY() {
        return (root, query, cb) ->
                cb.equal(root.get("useYn"), "Y");
    }

    public static Specification<Terms> termsTypeEq(String termsType) {
        return (root, query, cb) ->
                termsType == null ? null : cb.equal(root.get("termsType"), termsType);
    }
}
