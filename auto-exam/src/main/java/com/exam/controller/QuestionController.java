package com.exam.controller;

import com.exam.entity.Question;
import com.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 题库管理控制器
 */
@Controller
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /** 题库列表 */
    @GetMapping("/list")
    public String list(@RequestParam(required = false) String subject,
                       @RequestParam(required = false) Integer type,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        // 空字符串视为不筛选
        if (subject != null && subject.trim().isEmpty()) subject = null;
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;

        Page<Question> questions = questionService.search(subject, type, keyword, page, 10);
        model.addAttribute("questions", questions);
        model.addAttribute("subjects", questionService.findAllSubjects());
        model.addAttribute("currentSubject", subject);
        model.addAttribute("currentType", type);
        model.addAttribute("keyword", keyword);
        return "question/list";
    }

    /** 添加题目页面 */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("question", new Question());
        model.addAttribute("subjects", questionService.findAllSubjects());
        return "question/form";
    }

    /** 编辑题目页面 */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("question", questionService.findById(id));
        model.addAttribute("subjects", questionService.findAllSubjects());
        return "question/form";
    }

    /** 保存题目 */
    @PostMapping("/save")
    public String save(@ModelAttribute Question question, RedirectAttributes redirectAttributes) {
        try {
            questionService.save(question);
            redirectAttributes.addFlashAttribute("success", "题目保存成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "保存失败：" + e.getMessage());
        }
        return "redirect:/question/list";
    }

    /** 删除题目 */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            questionService.delete(id);
            redirectAttributes.addFlashAttribute("success", "题目已删除！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败：" + e.getMessage());
        }
        return "redirect:/question/list";
    }

    /** 导入示例题目 */
    @PostMapping("/import-sample")
    public String importSample(RedirectAttributes redirectAttributes) {
        questionService.importSampleQuestions();
        redirectAttributes.addFlashAttribute("success", "示例题目导入完成！");
        return "redirect:/question/list";
    }
}
