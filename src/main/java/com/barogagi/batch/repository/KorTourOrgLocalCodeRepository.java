package com.barogagi.batch.repository;

import com.barogagi.batch.dto.WeatherGridDTO;
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

    @Query("""
            SELECT code
            FROM KorTourOrgLocalCode code
            WHERE code.areaCd = :areaCd
            AND code.sigunguCd = :sigunguCd
            """)
    KorTourOrgLocalCode findLocalCodeInfo(@Param("areaCd") String areaCd,
                                          @Param("sigunguCd") String sigunguCd);


    @Query("""
    SELECT DISTINCT new com.barogagi.batch.dto.WeatherGridDTO(
        k.weatherNx,
        k.weatherNy
    )
    FROM KorTourOrgLocalCode k
    WHERE k.type = :type
""")
    List<WeatherGridDTO> findDistinctWeatherGrid(@Param("type") String type);
}
