package com.exam.repository;

import com.exam.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** 按科目查询 */
    List<Question> findBySubject(String subject);

    /** 按科目和类型查询 */
    List<Question> findBySubjectAndType(String subject, Integer type);

    /** 按科目、类型和难度查询 */
    List<Question> findBySubjectAndTypeAndDifficulty(String subject, Integer type, Integer difficulty);

    /** 按科目分页查询 */
    Page<Question> findBySubject(String subject, Pageable pageable);

    /** 搜索：科目+类型+关键词 */
    @Query("SELECT q FROM Question q WHERE (:subject IS NULL OR q.subject = :subject) " +
           "AND (:type IS NULL OR q.type = :type) " +
           "AND (:keyword IS NULL OR q.content LIKE %:keyword%)")
    Page<Question> searchQuestions(@Param("subject") String subject,
                                   @Param("type") Integer type,
                                   @Param("keyword") String keyword,
                                   Pageable pageable);

    /** 统计某科目各类型题目数量 */
    @Query("SELECT q.type, COUNT(q) FROM Question q WHERE q.subject = :subject GROUP BY q.type")
    List<Object[]> countBySubjectGroupByType(@Param("subject") String subject);

    /** 查询所有科目 */
    @Query("SELECT DISTINCT q.subject FROM Question q")
    List<String> findAllSubjects();
}
