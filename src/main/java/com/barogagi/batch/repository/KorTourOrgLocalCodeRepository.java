package com.barogagi.batch.repository;

import com.barogagi.batch.entity.KorTourOrgLocalCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KorTourOrgLocalCodeRepository extends JpaRepository<KorTourOrgLocalCode, Long> {

    @Query("""
            SELECT code
            FROM KorTourOrgLocalCode code
            WHERE code.type = :type
            """)
    List<KorTourOrgLocalCode> findLocalCode(@Param("type") String type);
}
