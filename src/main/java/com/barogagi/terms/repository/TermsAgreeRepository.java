package com.barogagi.terms.repository;

import com.barogagi.terms.domain.TermsAgree;
import com.barogagi.terms.domain.TermsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TermsAgreeRepository extends JpaRepository<TermsAgree, TermsId>, JpaSpecificationExecutor<TermsAgree> {
}
