package com.exam.controller;

import com.exam.service.PaperService;
import com.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

/**
 * API接口：提供统计数据给前端图表使用
 */
@RestController
public class ApiController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private PaperService paperService;

    /** 获取系统统计数据 */
    @GetMapping("/api/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("questionCount", questionService.count());
        stats.put("paperCount", paperService.count());
        stats.put("subjects", questionService.findAllSubjects());
        return stats;
    }

    /** 获取题库各科目统计 */
    @GetMapping("/api/question-stats")
    public List<Map<String, Object>> getQuestionStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        String[] typeNames = {"单选题", "多选题", "判断题", "填空题", "简答题"};

        for (String subject : questionService.findAllSubjects()) {
            Map<String, Object> subjectData = new HashMap<>();
            subjectData.put("subject", subject);

            List<Object[]> counts = questionService.countBySubjectGroupByType(subject);
            Map<Integer, Long> typeCountMap = new HashMap<>();
            for (Object[] row : counts) {
                typeCountMap.put((Integer) row[0], (Long) row[1]);
            }

            Map<String, Long> typeMap = new LinkedHashMap<>();
            for (int i = 0; i < typeNames.length; i++) {
                typeMap.put(typeNames[i], typeCountMap.getOrDefault(i + 1, 0L));
            }
            subjectData.put("types", typeMap);
            result.add(subjectData);
        }
        return result;
    }
}
