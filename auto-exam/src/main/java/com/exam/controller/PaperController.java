package com.exam.controller;

import com.exam.entity.Paper;
import com.exam.entity.PaperQuestion;
import com.exam.entity.Question;
import com.exam.service.GeneticAlgorithmService;
import com.exam.service.PaperService;
import com.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * 试卷管理控制器
 */
@Controller
@RequestMapping("/paper")
public class PaperController {

    @Autowired
    private PaperService paperService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private GeneticAlgorithmService gaService;

    /** 试卷列表 */
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("papers", paperService.findAll());
        return "paper/list";
    }

    /** 自动组卷页面 */
    @GetMapping("/generate")
    public String generateForm(Model model) {
        model.addAttribute("subjects", questionService.findAllSubjects());
        return "paper/generate";
    }

    /** 执行自动组卷 */
    @PostMapping("/generate")
    public String doGenerate(
            @RequestParam String subject,
            @RequestParam String paperName,
            @RequestParam(defaultValue = "120") int duration,
            @RequestParam(defaultValue = "100") int totalScore,
            @RequestParam(defaultValue = "10") int singleChoice,
            @RequestParam(defaultValue = "5") int multiChoice,
            @RequestParam(defaultValue = "5") int judge,
            @RequestParam(defaultValue = "5") int fill,
            @RequestParam(defaultValue = "3") int shortAnswer,
            @RequestParam(defaultValue = "30") int easyRatio,
            @RequestParam(defaultValue = "50") int mediumRatio,
            @RequestParam(defaultValue = "20") int hardRatio,
            RedirectAttributes redirectAttributes) {
        try {
            GeneticAlgorithmService.PaperConstraint constraint =
                    GeneticAlgorithmService.PaperConstraint.builder()
                            .subject(subject)
                            .paperName(paperName)
                            .totalScore(totalScore)
                            .duration(duration)
                            .typeCounts(singleChoice, multiChoice, judge, fill, shortAnswer)
                            .difficultyRatio(easyRatio, mediumRatio, hardRatio)
                            .build();

            Paper paper = gaService.generatePaper(constraint);
            redirectAttributes.addFlashAttribute("success",
                    "试卷生成成功！「" + paper.getName() + "」共" + paper.getTotalScore() + "分");
            return "redirect:/paper/view/" + paper.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "组卷失败：" + e.getMessage());
            return "redirect:/paper/generate";
        }
    }

    /** 查看试卷详情 */
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        Paper paper = paperService.findById(id)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));
        List<PaperQuestion> allQuestions = paperService.getPaperQuestions(id);

        // 按题型分类
        List<PaperQuestion> singleChoice = new ArrayList<>();
        List<PaperQuestion> multiChoice = new ArrayList<>();
        List<PaperQuestion> judge = new ArrayList<>();
        List<PaperQuestion> fill = new ArrayList<>();
        List<PaperQuestion> shortAnswer = new ArrayList<>();

        for (PaperQuestion pq : allQuestions) {
            if (pq.getQuestion() == null) continue;
            switch (pq.getQuestion().getType()) {
                case 1: singleChoice.add(pq); break;
                case 2: multiChoice.add(pq); break;
                case 3: judge.add(pq); break;
                case 4: fill.add(pq); break;
                case 5: shortAnswer.add(pq); break;
            }
        }

        model.addAttribute("paper", paper);
        model.addAttribute("singleChoiceQuestions", singleChoice);
        model.addAttribute("multiChoiceQuestions", multiChoice);
        model.addAttribute("judgeQuestions", judge);
        model.addAttribute("fillQuestions", fill);
        model.addAttribute("shortAnswerQuestions", shortAnswer);
        return "paper/view";
    }

    /** 删除试卷 */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            paperService.deletePaper(id);
            redirectAttributes.addFlashAttribute("success", "试卷已删除！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败：" + e.getMessage());
        }
        return "redirect:/paper/list";
    }

    /** 发布试卷 */
    @GetMapping("/publish/{id}")
    public String publish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        paperService.updateStatus(id, 1);
        redirectAttributes.addFlashAttribute("success", "试卷已发布！");
        return "redirect:/paper/list";
    }
}
