package com.exam.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷实体类
 */
@Data
@Entity
@Table(name = "paper")
public class Paper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 试卷名称 */
    @Column(nullable = false, length = 200)
    private String name;

    /** 所属科目 */
    @Column(nullable = false, length = 100)
    private String subject;

    /** 总分 */
    @Column(nullable = false)
    private Integer totalScore;

    /** 考试时长（分钟） */
    @Column(nullable = false)
    private Integer duration = 120;

    /** 难度系数 0-1 */
    private Double difficultyFactor;

    /** 试卷状态：0-草稿 1-已发布 */
    private Integer status = 0;

    /** 组卷方式：1-手动 2-自动（遗传算法） */
    private Integer generateType = 2;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionOrder ASC")
    private List<PaperQuestion> paperQuestions;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
