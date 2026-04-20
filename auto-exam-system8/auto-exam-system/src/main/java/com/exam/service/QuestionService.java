package com.exam.service;

import com.exam.entity.Question;
import com.exam.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    public Question save(Question question) {
        return questionRepository.save(question);
    }

    public void delete(Long id) {
        questionRepository.deleteById(id);
    }

    public Question findById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("题目不存在，ID: " + id));
    }

    public Page<Question> search(String subject, Integer type, String keyword, int page, int size) {
        return questionRepository.searchQuestions(subject, type, keyword,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime")));
    }

    public List<Question> findBySubjectAndTypeAndDifficulty(String subject, Integer type, Integer difficulty) {
        return questionRepository.findBySubjectAndTypeAndDifficulty(subject, type, difficulty);
    }

    public List<Question> findBySubjectAndType(String subject, Integer type) {
        return questionRepository.findBySubjectAndType(subject, type);
    }

    public List<String> findAllSubjects() {
        return questionRepository.findAllSubjects();
    }

    public long count() {
        return questionRepository.count();
    }

    /** 获取某科目各类型题目数量统计 */
    public List<Object[]> countBySubjectGroupByType(String subject) {
        return questionRepository.countBySubjectGroupByType(subject);
    }

    /** 批量导入示例题目（高等数学 + 数据结构，每科44题共88题） */
    public void importSampleQuestions() {
        if (questionRepository.count() > 0) return;

        // ==========================================
        //  高等数学（44题：单选15 + 多选8 + 判断8 + 填空8 + 简答5）
        // ==========================================

        // ---- 单选题 15道 ----
        addQuestion(1, 1, "高等数学", "极限与连续",
                "求极限 lim(x→0) (sin x) / x 的值是多少？",
                "[\"A. 0\", \"B. 1\", \"C. ∞\", \"D. 不存在\"]", "B", "利用重要极限公式 lim(x→0) (sin x) / x = 1", 2);

        addQuestion(1, 1, "高等数学", "极限与连续",
                "函数 f(x) = 1/x 在 x=0 处是？",
                "[\"A. 连续\", \"B. 无穷间断点\", \"C. 可去间断点\", \"D. 跳跃间断点\"]", "B",
                "f(x)在x=0处无定义且趋于无穷，属于无穷间断点", 2);

        addQuestion(1, 1, "高等数学", "导数与微分",
                "函数 f(x) = eˣ 的导数是？",
                "[\"A. eˣ\", \"B. xeˣ⁻¹\", \"C. ln x\", \"D. 1/x\"]", "A", "指数函数 eˣ 的导数等于其本身", 2);

        addQuestion(1, 1, "高等数学", "导数与微分",
                "函数 f(x) = ln x 的导数是？",
                "[\"A. 1/x\", \"B. x\", \"C. eˣ\", \"D. -1/x²\"]", "A", "ln x 的导数为 1/x", 2);

        addQuestion(1, 1, "高等数学", "积分",
                "定积分 ∫₀¹ x² dx 的值是？",
                "[\"A. 1/3\", \"B. 1/2\", \"C. 1\", \"D. 2\"]", "A",
                "∫₀¹ x² dx = [x³/3]₀¹ = 1/3", 2);

        addQuestion(1, 2, "高等数学", "极限与连续",
                "求极限 lim(x→0) (1 - cos x) / x² 的值。",
                "[\"A. 0\", \"B. 1/2\", \"C. 1\", \"D. ∞\"]", "B",
                "利用等价无穷小：1 - cos x ~ x²/2", 2);

        addQuestion(1, 2, "高等数学", "极限与连续",
                "求极限 lim(x→∞) (1 + 1/x)ˣ 的值。",
                "[\"A. 1\", \"B. e\", \"C. 0\", \"D. ∞\"]", "B",
                "这是自然对数底 e 的定义", 3);

        addQuestion(1, 2, "高等数学", "导数与微分",
                "设 y = sin(x²)，则 y' = ？",
                "[\"A. cos(x²)\", \"B. 2x·cos(x²)\", \"C. 2x·sin(x²)\", \"D. -2x·cos(x²)\"]", "B",
                "链式法则：y' = cos(x²) · 2x", 2);

        addQuestion(1, 2, "高等数学", "导数与微分",
                "设 f(x) = x·ln x，则 f'(1) = ？",
                "[\"A. 0\", \"B. 1\", \"C. -1\", \"D. e\"]", "B",
                "f'(x) = ln x + x·(1/x) = ln x + 1, f'(1) = 0 + 1 = 1", 2);

        addQuestion(1, 2, "高等数学", "积分",
                "不定积分 ∫1/(1+x²) dx = ？",
                "[\"A. arctan x + C\", \"B. arcsin x + C\", \"C. ln(1+x²) + C\", \"D. 1/(1+x²) + C\"]", "A",
                "arctan x 的导数为 1/(1+x²)", 2);

        addQuestion(1, 2, "高等数学", "级数",
                "等比级数 ∑(n=0→∞) rⁿ 当 |r| < 1 时的和为？",
                "[\"A. r/(1-r)\", \"B. 1/(1-r)\", \"C. 1/r\", \"D. 发散\"]", "B",
                "等比级数求和公式 S = a/(1-r)，首项a=1", 2);

        addQuestion(1, 3, "高等数学", "极限与连续",
                "lim(x→0⁺) x·ln x = ？",
                "[\"A. 0\", \"B. -1\", \"C. ∞\", \"D. 不存在\"]", "A",
                "令 t = 1/x，转化为 lim(t→∞) (-ln t)/t = 0", 3);

        addQuestion(1, 3, "高等数学", "导数与微分",
                "函数 f(x) = x^(1/x) 在 x = ？处取得最大值。",
                "[\"A. 1\", \"B. e\", \"C. 2\", \"D. π\"]", "B",
                "令 y = x^(1/x), ln y = ln x / x, 求导得 x = e 时取极值", 3);

        addQuestion(1, 3, "高等数学", "积分",
                "广义积分 ∫₁⁺∞ 1/x² dx 的值为？",
                "[\"A. 0\", \"B. 1\", \"C. 1/2\", \"D. 发散\"]", "B",
                "∫₁⁺∞ 1/x² dx = [-1/x]₁⁺∞ = 0 - (-1) = 1", 3);

        addQuestion(1, 3, "高等数学", "导数与微分",
                "设 f(x) 在 x₀ 处可导，则 lim(h→0) [f(x₀+h) - f(x₀-h)] / (2h) = ？",
                "[\"A. f'(x₀)\", \"B. 2f'(x₀)\", \"C. 0\", \"D. f''(x₀)\"]", "A",
                "这是导数的对称差商定义，等价于 f'(x₀)", 3);

        // ---- 多选题 8道 ----
        addQuestion(2, 1, "高等数学", "极限与连续",
                "下列哪些函数在 x=0 处连续？",
                "[\"A. f(x) = x²\", \"B. f(x) = |x|\", \"C. f(x) = 1/x\", \"D. f(x) = sin x\"]", "A,B,D",
                "1/x 在 x=0 处无定义，不连续", 3);

        addQuestion(2, 1, "高等数学", "导数与微分",
                "以下哪些函数的导数是 2x？",
                "[\"A. f(x) = x²\", \"B. f(x) = x² + 1\", \"C. f(x) = x² + C\", \"D. f(x) = 2x\"]", "A,B,C",
                "x² + 常数的导数都是2x，2x的导数是2", 3);

        addQuestion(2, 2, "高等数学", "积分",
                "下列哪些不定积分的计算是正确的？",
                "[\"A. ∫x²dx = x³/3 + C\", \"B. ∫1/x dx = ln|x| + C\", \"C. ∫eˣdx = eˣ + C\", \"D. ∫sin x dx = cos x + C\"]",
                "A,B,C", "∫sin x dx = -cos x + C", 3);

        addQuestion(2, 2, "高等数学", "导数与微分",
                "以下哪些函数在 x=0 处可导？",
                "[\"A. f(x) = x²\", \"B. f(x) = |x|\", \"C. f(x) = x³\", \"D. f(x) = sin x\"]", "A,C,D",
                "f(x) = |x| 在 x=0 处左导数和右导数不相等", 3);

        addQuestion(2, 1, "高等数学", "极限与连续",
                "以下哪些是无穷小量（当 x→0 时）？",
                "[\"A. x\", \"B. x²\", \"C. sin x\", \"D. 1/x\"]", "A,B,C",
                "1/x 当 x→0 时趋于无穷大，不是无穷小量", 3);

        addQuestion(2, 2, "高等数学", "级数",
                "以下哪些级数收敛？",
                "[\"A. ∑1/n²\", \"B. ∑1/n\", \"C. ∑(1/2)ⁿ\", \"D. ∑1/√n\"]", "A,C",
                "∑1/n 和 ∑1/√n 都发散（p级数p≤1）", 3);

        addQuestion(2, 3, "高等数学", "微分方程",
                "以下哪些是一阶微分方程？",
                "[\"A. dy/dx + y = 0\", \"B. y'' + y = 0\", \"C. dy/dx = x²\", \"D. d²y/dx² + dy/dx = 0\"]", "A,C",
                "一阶微分方程的最高阶导数为1", 3);

        addQuestion(2, 3, "高等数学", "多元函数",
                "以下关于偏导数说法正确的是？",
                "[\"A. 偏导数存在则函数连续\", \"B. 函数连续偏导数不一定存在\", \"C. 偏导数连续则函数可微\", \"D. 可微则偏导数存在\"]", "B,C,D",
                "偏导数存在不能推出函数连续（反例见经典反例）", 3);

        // ---- 判断题 8道 ----
        addQuestion(3, 1, "高等数学", "导数与微分",
                "若 f(x) = x³，则 f'(2) = 12。",
                null, "正确", "f'(x) = 3x², f'(2) = 3×4 = 12", 2);

        addQuestion(3, 1, "高等数学", "积分",
                "若 f(x) 在 [a,b] 上连续，则 f(x) 在 [a,b] 上一定可积。",
                null, "正确", "连续函数在闭区间上一定可积，这是定积分存在定理", 2);

        addQuestion(3, 2, "高等数学", "导数与微分",
                "若 f'(x) > 0，则 f(x) 在该区间上单调递增。",
                null, "正确", "导数大于0是函数单调递增的充分条件", 2);

        addQuestion(3, 2, "高等数学", "极限与连续",
                "两个无穷小量之商一定是无穷小量。",
                null, "错误", "例如 x/x = 1 不是无穷小量；x²/x = x 是无穷小量，但 x/x² = 1/x 是无穷大量", 2);

        addQuestion(3, 2, "高等数学", "积分",
                "定积分的值与积分变量的记号无关。",
                null, "正确", "∫f(x)dx = ∫f(t)dt，积分值只与被积函数和积分区间有关", 2);

        addQuestion(3, 3, "高等数学", "多元函数",
                "二元函数 z = x² + y² 在 (0,0) 处取得极小值。",
                null, "正确", "z在(0,0)处取得最小值0", 2);

        addQuestion(3, 3, "高等数学", "级数",
                "级数 ∑(-1)ⁿ/n 是绝对收敛的。",
                null, "错误", "∑|(-1)ⁿ/n| = ∑1/n 发散，所以是条件收敛", 2);

        addQuestion(3, 1, "高等数学", "极限与连续",
                "初等函数在其定义域内都是连续的。",
                null, "正确", "初等函数在其定义区间内连续", 2);

        // ---- 填空题 8道 ----
        addQuestion(4, 1, "高等数学", "导数与微分",
                "设 y = ln(x² + 1)，则 dy/dx = ______。",
                null, "2x / (x² + 1)", "利用链式法则：y' = 1/(x²+1) · 2x", 3);

        addQuestion(4, 1, "高等数学", "积分",
                "不定积分 ∫cos x dx = _______。",
                null, "sin x + C", "cos x 的原函数是 sin x", 2);

        addQuestion(4, 1, "高等数学", "极限与连续",
                "lim(x→0) sin(3x) / x = _______。",
                null, "3", "利用等价无穷小 sin(3x) ~ 3x", 2);

        addQuestion(4, 2, "高等数学", "导数与微分",
                "设 y = e^(2x)，则 y'' = _______。",
                null, "4e^(2x)", "y' = 2e^(2x), y'' = 4e^(2x)", 2);

        addQuestion(4, 2, "高等数学", "积分",
                "定积分 ∫₀^π sin x dx = _______。",
                null, "2", "[-cos x]₀^π = -cosπ - (-cos0) = 1 + 1 = 2", 2);

        addQuestion(4, 2, "高等数学", "导数与微分",
                "曲线 y = x² 在点 (1,1) 处的切线斜率为 _______。",
                null, "2", "y' = 2x, 在x=1处 y' = 2", 2);

        addQuestion(4, 3, "高等数学", "极限与连续",
                "函数 f(x) = (x²-1)/(x-1) 在 x=1 处的间断点类型为 _______ 间断点。",
                null, "可去", "f(x) = x+1 (x≠1), 极限为2但函数在x=1无定义", 3);

        addQuestion(4, 3, "高等数学", "多元函数",
                "设 z = x²y + xy²，则 ∂z/∂x = _______。",
                null, "2xy + y²", "对x求偏导，y视为常数", 3);

        // ---- 简答题 5道 ----
        addQuestion(5, 1, "高等数学", "积分",
                "简述定积分的几何意义。",
                null, "定积分表示函数曲线与x轴之间围成的有向面积",
                "定积分的几何意义是曲线y=f(x)与x轴在[a,b]区间上围成的曲边梯形的有向面积", 5);

        addQuestion(5, 2, "高等数学", "极限与连续",
                "简述夹逼准则的内容及其适用条件。",
                null, "若 g(x) ≤ f(x) ≤ h(x)，且 lim g(x) = lim h(x) = A，则 lim f(x) = A",
                "夹逼准则需要三个函数在某去心邻域内满足不等式关系", 5);

        addQuestion(5, 2, "高等数学", "导数与微分",
                "简述罗尔定理的条件和结论。",
                null, "条件：f(x)在[a,b]连续、在(a,b)可导、f(a)=f(b)；结论：至少存在一点ξ∈(a,b)使得f'(ξ)=0",
                "罗尔定理是拉格朗日中值定理的特例", 5);

        addQuestion(5, 3, "高等数学", "极限与连续",
                "函数 f(x) = x³ 在全体实数上是否一致连续？说明理由。",
                null, "不一致连续。取 ε=1，对任意 δ>0，取 x=N, y=N+δ/2N，当N充分大时 |f(x)-f(y)| > ε",
                "一致连续要求增长不能太快，x³在无穷远处增长过快", 5);

        addQuestion(5, 3, "高等数学", "积分",
                "解释为什么 ∫₀¹ 1/x dx 是发散的广义积分。",
                null, "因为被积函数 1/x 在 x=0 处无定义，且 lim(x→0⁺) ∫ₓ¹ 1/t dt = lim(-ln x) = +∞，积分发散",
                "被积函数在积分区间端点处趋于无穷大", 5);

        // ==========================================
        //  数据结构（44题：单选15 + 多选8 + 判断8 + 填空8 + 简答5）
        // ==========================================

        // ---- 单选题 15道 ----
        addQuestion(1, 1, "数据结构", "线性表",
                "在一个长度为n的顺序表中，删除第i个元素的时间复杂度为？",
                "[\"A. O(1)\", \"B. O(n)\", \"C. O(log n)\", \"D. O(n²)\"]", "B",
                "删除第i个元素需要将后面的n-i个元素各向前移动一位", 2);

        addQuestion(1, 1, "数据结构", "栈",
                "栈的特点是？",
                "[\"A. 先进先出\", \"B. 后进先出\", \"C. 随机存取\", \"D. 顺序存取\"]", "B",
                "栈是一种后进先出(LIFO)的线性数据结构", 2);

        addQuestion(1, 1, "数据结构", "树",
                "前序遍历的顺序是？",
                "[\"A. 根-左-右\", \"B. 左-根-右\", \"C. 左-右-根\", \"D. 根-右-左\"]", "A",
                "前序（先序）遍历：根→左子树→右子树", 2);

        addQuestion(1, 1, "数据结构", "图",
                "具有n个顶点的无向图，最多有几条边？",
                "[\"A. n\", \"B. n-1\", \"C. n(n-1)/2\", \"D. n²\"]", "C",
                "无向完全图的边数为 n(n-1)/2", 2);

        addQuestion(1, 1, "数据结构", "排序",
                "对n个元素进行快速排序，最坏情况下的时间复杂度为？",
                "[\"A. O(n)\", \"B. O(n log n)\", \"C. O(n²)\", \"D. O(log n)\"]", "C",
                "最坏情况（已排序数组）下每次只能划分出一个子数组", 2);

        addQuestion(1, 2, "数据结构", "树",
                "一棵完全二叉树有1000个节点，其叶子节点的个数为？",
                "[\"A. 500\", \"B. 499\", \"C. 501\", \"D. 498\"]", "A",
                "n0 = n/2（向下取整）= 500", 3);

        addQuestion(1, 2, "数据结构", "线性表",
                "在单链表中，要访问第i个元素，必须？",
                "[\"A. 直接访问\", \"B. 从头结点顺序查找\", \"C. 二分查找\", \"D. 随机访问\"]", "B",
                "链表不支持随机访问，只能从头顺序查找", 2);

        addQuestion(1, 2, "数据结构", "查找",
                "对1000个有序元素进行二分查找，最多需要比较几次？",
                "[\"A. 10\", \"B. 20\", \"C. 500\", \"D. 1000\"]", "A",
                "⌈log₂(1000)⌉ = ⌈9.97⌉ = 10", 2);

        addQuestion(1, 2, "数据结构", "哈希",
                "哈希查找的平均时间复杂度为？",
                "[\"A. O(1)\", \"B. O(log n)\", \"C. O(n)\", \"D. O(n²)\"]", "A",
                "理想情况下哈希查找的时间复杂度为O(1)", 2);

        addQuestion(1, 2, "数据结构", "排序",
                "以下排序算法中，空间复杂度最高的是？",
                "[\"A. 冒泡排序\", \"B. 快速排序\", \"C. 归并排序\", \"D. 插入排序\"]", "C",
                "归并排序需要O(n)额外空间", 2);

        addQuestion(1, 2, "数据结构", "队列",
                "循环队列解决了什么问题？",
                "[\"A. 队列溢出\", \"B. 假溢出\", \"C. 队列为空\", \"D. 数据丢失\"]", "B",
                "循环队列通过取模运算解决队列的假溢出问题", 2);

        addQuestion(1, 3, "数据结构", "树",
                "一棵有n个节点的二叉树，其深度最多为？",
                "[\"A. n\", \"B. n-1\", \"C. log₂n\", \"D. n/2\"]", "A",
                "斜树（每层一个节点）的深度为n", 3);

        addQuestion(1, 3, "数据结构", "图",
                "n个顶点的连通无向图至少需要几条边？",
                "[\"A. n\", \"B. n-1\", \"C. n(n-1)/2\", \"D. n²\"]", "B",
                "n个顶点的连通图至少需要n-1条边（生成树）", 2);

        addQuestion(1, 3, "数据结构", "排序",
                "堆排序的时间复杂度为？",
                "[\"A. O(n)\", \"B. O(n log n)\", \"C. O(n²)\", \"D. O(log n)\"]", "B",
                "建堆O(n) + 每次调整O(log n)，共n次，总O(n log n)", 2);

        addQuestion(1, 3, "数据结构", "查找",
                "对n个元素的AVL树进行查找的时间复杂度为？",
                "[\"A. O(1)\", \"B. O(log n)\", \"C. O(n)\", \"D. O(n log n)\"]", "B",
                "AVL树始终保持平衡，树高为O(log n)", 2);

        // ---- 多选题 8道 ----
        addQuestion(2, 1, "数据结构", "排序",
                "以下哪些排序算法的平均时间复杂度为 O(n log n)？",
                "[\"A. 快速排序\", \"B. 归并排序\", \"C. 堆排序\", \"D. 冒泡排序\"]", "A,B,C",
                "冒泡排序平均时间复杂度为O(n²)", 3);

        addQuestion(2, 1, "数据结构", "线性表",
                "以下哪些是线性数据结构？",
                "[\"A. 栈\", \"B. 队列\", \"C. 二叉树\", \"D. 链表\"]", "A,B,D",
                "二叉树是非线性数据结构", 3);

        addQuestion(2, 2, "数据结构", "图",
                "以下哪些属于图的存储结构？",
                "[\"A. 邻接矩阵\", \"B. 邻接表\", \"C. 十字链表\", \"D. 二叉链表\"]", "A,B,C",
                "二叉链表用于存储二叉树，不是图的存储结构", 3);

        addQuestion(2, 2, "数据结构", "树",
                "关于满二叉树，以下说法正确的是？",
                "[\"A. 每层节点数都达到最大\", \"B. 叶子节点在同一层\", \"C. 深度为k时有2^k-1个节点\", \"D. 一定是完全二叉树\"]",
                "A,B,C,D", "满二叉树是完全二叉树的特殊情况", 3);

        addQuestion(2, 2, "数据结构", "查找",
                "以下关于二分查找说法正确的是？",
                "[\"A. 要求数据有序\", \"B. 时间复杂度 O(log n)\", \"C. 适用于链表\", \"D. 只能用于数组\"]", "A,B,D",
                "二分查找需要随机访问，不适用于链表", 3);

        addQuestion(2, 1, "数据结构", "排序",
                "以下哪些是稳定的排序算法？",
                "[\"A. 冒泡排序\", \"B. 归并排序\", \"C. 快速排序\", \"D. 插入排序\"]", "A,B,D",
                "快速排序是不稳定排序", 3);

        addQuestion(2, 3, "数据结构", "图",
                "以下关于最小生成树说法正确的是？",
                "[\"A. Prim算法适合稠密图\", \"B. Kruskal算法适合稀疏图\", \"C. 最小生成树可能不唯一\", \"D. 最小生成树有n-1条边\"]",
                "A,B,C,D", "最小生成树的边数等于顶点数减1", 3);

        addQuestion(2, 3, "数据结构", "算法",
                "以下关于动态规划说法正确的是？",
                "[\"A. 具有最优子结构\", \"B. 存在重叠子问题\", \"C. 可以用递归实现\", \"D. 时间复杂度一定比贪心高\"]", "A,B,C",
                "动态规划的时间复杂度不一定比贪心高", 3);

        // ---- 判断题 8道 ----
        addQuestion(3, 1, "数据结构", "图",
                "深度优先搜索(DFS)可以用来判断图中是否存在环。",
                null, "正确", "DFS通过检测回边（指向已访问祖先节点的边）来判断是否有环", 2);

        addQuestion(3, 1, "数据结构", "排序",
                "冒泡排序是一种稳定的排序算法。",
                null, "正确", "冒泡排序相等元素不会交换位置，是稳定排序", 2);

        addQuestion(3, 1, "数据结构", "线性表",
                "链表的插入和删除操作时间复杂度一定为 O(1)。",
                null, "错误", "需要先找到位置，查找的时间复杂度为O(n)", 2);

        addQuestion(3, 2, "数据结构", "树",
                "任意一棵二叉树的叶子节点数等于度为2的节点数加1。",
                null, "正确", "n0 = n2 + 1，这是二叉树的基本性质", 2);

        addQuestion(3, 2, "数据结构", "哈希",
                "哈希表中不可能出现冲突。",
                null, "错误", "不同的关键字可能映射到同一个哈希地址，冲突不可避免但可以处理", 2);

        addQuestion(3, 2, "数据结构", "排序",
                "快速排序在任何情况下都是最优的排序算法。",
                null, "错误", "快速排序最坏情况时间复杂度为O(n²)，对小规模数据不如插入排序", 2);

        addQuestion(3, 3, "数据结构", "图",
                "Dijkstra算法可以处理含有负权边的图。",
                null, "错误", "Dijkstra算法不能处理负权边，需使用Bellman-Ford算法", 2);

        addQuestion(3, 3, "数据结构", "树",
                "AVL树的任意子树仍然是AVL树。",
                null, "正确", "AVL树具有递归性质，子树也满足平衡条件", 2);

        // ---- 填空题 8道 ----
        addQuestion(4, 1, "数据结构", "栈",
                "栈的特点是 _______ （后进先出/先进先出）。",
                null, "后进先出", "栈是一种后进先出(LIFO)的线性数据结构", 2);

        addQuestion(4, 1, "数据结构", "哈希",
                "哈希表中处理冲突的常用方法有链地址法和 _______ 法。",
                null, "开放地址", "开放地址法包括线性探测、二次探测等", 2);

        addQuestion(4, 1, "数据结构", "树",
                "二叉树的中序遍历顺序为：左子树 → _______ → 右子树。",
                null, "根节点", "中序遍历：左-根-右", 2);

        addQuestion(4, 2, "数据结构", "图",
                "具有n个顶点的无向连通图至少有 _______ 条边。",
                null, "n-1", "n个顶点的连通图至少需要n-1条边（即生成树）", 2);

        addQuestion(4, 2, "数据结构", "排序",
                "快速排序的基本思想是 _______ 。",
                null, "分治法", "快速排序通过一趟排序将数据分为两部分，递归处理", 2);

        addQuestion(4, 2, "数据结构", "线性表",
                "在双向链表中，每个节点包含 _______ 个指针域。",
                null, "2", "双向链表节点有prior和next两个指针", 2);

        addQuestion(4, 3, "数据结构", "树",
                "对n个节点的AVL树进行查找的时间复杂度为 _______。",
                null, "O(log n)", "AVL树保持平衡，树高为O(log n)", 2);

        addQuestion(4, 3, "数据结构", "算法",
                "Prim算法的时间复杂度为 _______ （使用邻接矩阵时）。",
                null, "O(n²)", "Prim算法使用邻接矩阵时，每次找最小边需要O(n)，共n次", 3);

        // ---- 简答题 5道 ----
        addQuestion(5, 1, "数据结构", "树",
                "简述二叉搜索树(BST)的性质。",
                null, "左子树上所有节点的值小于根节点的值；右子树上所有节点的值大于根节点的值；左右子树也分别为二叉搜索树",
                "BST是重要的数据结构，支持高效的查找操作", 5);

        addQuestion(5, 2, "数据结构", "算法",
                "简述动态规划的基本思想及其与分治法的区别。",
                null, "动态规划将问题分解为重叠子问题，通过存储子问题的解避免重复计算；分治法的子问题是不重叠的",
                "动态规划的核心是最优子结构和重叠子问题", 5);

        addQuestion(5, 2, "数据结构", "图",
                "简述深度优先搜索(DFS)和广度优先搜索(BFS)的区别。",
                null, "DFS沿着一条路径深入到底再回溯，使用栈或递归；BFS逐层扩展，使用队列。DFS适合判断连通性、拓扑排序；BFS适合求最短路径",
                "DFS和BFS是图的两种基本遍历方式", 5);

        addQuestion(5, 3, "数据结构", "排序",
                "比较快速排序和归并排序的优缺点。",
                null, "快排：原地排序O(log n)空间，平均O(n log n)但最坏O(n²)，不稳定；归并：稳定排序，最坏O(n log n)但需要O(n)额外空间",
                "实际中快排通常更快（常数因子小），归并排序适合外部排序", 5);

        addQuestion(5, 3, "数据结构", "图",
                "简述Dijkstra算法的基本步骤。",
                null, "1.初始化起点距离为0其余为∞；2.选取距离最小的未访问顶点；3.更新其邻居的距离；4.标记已访问；5.重复2-4直到所有顶点访问完",
                "Dijkstra是经典的单源最短路径算法", 5);

        System.out.println("示例题目导入完成，共导入 " + questionRepository.count() + " 道题");
    }

    private void addQuestion(Integer type, Integer difficulty, String subject, String chapter,
                             String content, String options, String answer, String analysis, Integer score) {
        Question q = new Question();
        q.setType(type);
        q.setDifficulty(difficulty);
        q.setSubject(subject);
        q.setChapter(chapter);
        q.setContent(content);
        q.setOptions(options);
        q.setAnswer(answer);
        q.setAnalysis(analysis);
        q.setScore(score);
        questionRepository.save(q);
    }
}
