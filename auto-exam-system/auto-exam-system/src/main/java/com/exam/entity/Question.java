package com.exam.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 试题实体类
 */
@Data
@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 题目类型：1-单选题 2-多选题 3-判断题 4-填空题 5-简答题 */
    @Column(nullable = false)
    private Integer type;

    /** 难度：1-简单 2-中等 3-困难 */
    @Column(nullable = false)
    private Integer difficulty;

    /** 所属科目/课程 */
    @Column(nullable = false, length = 100)
    private String subject;

    /** 知识点/章节 */
    @Column(length = 200)
    private String chapter;

    /** 题目内容 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 选项（JSON格式，用于选择题） */
    @Column(columnDefinition = "TEXT")
    private String options;

    /** 正确答案 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    /** 解析 */
    @Column(columnDefinition = "TEXT")
    private String analysis;

    /** 分值 */
    @Column(nullable = false)
    private Integer score = 2;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        if (this.score == null) {
            this.score = 2;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
