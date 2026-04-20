package com.exam.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import javax.persistence.*;

/**
 * 试卷-试题关联实体类
 */
@Data
@Entity
@Table(name = "paper_question")
public class PaperQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    @JsonIgnore
    private Paper paper;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /** 题目在试卷中的序号 */
    @Column(nullable = false)
    private Integer questionOrder;

    /** 该题在本试卷中的分值（可覆盖默认分值） */
    @Column(nullable = false)
    private Integer score;
}
