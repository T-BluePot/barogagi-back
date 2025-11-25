package com.barogagi.tag.command.repository;
import com.barogagi.tag.command.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository  extends JpaRepository<Tag, Integer> {
//    Optional<Object> findById(Integer tagNum);
}

