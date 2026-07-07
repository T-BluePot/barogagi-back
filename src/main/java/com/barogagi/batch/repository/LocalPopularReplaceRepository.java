package com.barogagi.batch.repository;

import com.barogagi.batch.entity.LocalPopularReplace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalPopularReplaceRepository extends JpaRepository<LocalPopularReplace, Long> {

    boolean existsByBaseYm(String baseYm);

}
