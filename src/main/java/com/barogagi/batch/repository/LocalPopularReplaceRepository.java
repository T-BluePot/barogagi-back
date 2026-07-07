package com.barogagi.batch.repository;

import com.barogagi.batch.entity.LocalPopularReplace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalPopularReplaceRepository extends JpaRepository<LocalPopularReplace, Long> {

    boolean existsByBaseYm(String baseYm);

    @Query("""
            SELECT lpr
            FROM LocalPopularReplace lpr
            WHERE lpr.areaCd = :areaCd
            AND lpr.signguCd = :sigunguCd
            """)
    List<LocalPopularReplace> findLocalPopularReplace(@Param("areaCd") String areaCd,
                                         @Param("sigunguCd") String sigunguCd);
}
