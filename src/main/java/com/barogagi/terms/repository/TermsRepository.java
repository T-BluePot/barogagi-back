package com.barogagi.terms.repository;

import com.barogagi.terms.domain.Terms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TermsRepository extends JpaRepository<Terms, Integer>, JpaSpecificationExecutor<Terms> {
}
