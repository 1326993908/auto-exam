package com.exam.repository;

import com.exam.entity.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaperRepository extends JpaRepository<Paper, Long> {
    List<Paper> findBySubject(String subject);
    List<Paper> findByStatus(Integer status);
}
