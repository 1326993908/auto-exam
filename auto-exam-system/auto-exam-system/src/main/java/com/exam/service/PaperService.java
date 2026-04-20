package com.exam.service;

import com.exam.entity.Paper;
import com.exam.entity.PaperQuestion;
import com.exam.repository.PaperRepository;
import com.exam.repository.PaperQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PaperService {

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private PaperQuestionRepository paperQuestionRepository;

    @Transactional(readOnly = true)
    public List<Paper> findAll() {
        return paperRepository.findAll();
    }

    public Optional<Paper> findById(Long id) {
        return paperRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<PaperQuestion> getPaperQuestions(Long paperId) {
        return paperQuestionRepository.findByPaperIdOrderByQuestionOrder(paperId);
    }

    @Transactional
    public void deletePaper(Long id) {
        paperQuestionRepository.deleteByPaperId(id);
        paperRepository.deleteById(id);
    }

    public Paper updateStatus(Long id, Integer status) {
        Paper paper = paperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));
        paper.setStatus(status);
        return paperRepository.save(paper);
    }

    public long count() {
        return paperRepository.count();
    }
}
