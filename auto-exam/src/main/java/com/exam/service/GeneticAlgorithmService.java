package com.exam.service;

import com.exam.entity.Paper;
import com.exam.entity.PaperQuestion;
import com.exam.entity.Question;
import com.exam.repository.PaperRepository;
import com.exam.repository.PaperQuestionRepository;
import com.exam.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 自动组卷服务 - 基于遗传算法
 *
 * 核心思想：
 * 将组卷问题转化为优化问题，每个染色体代表一套试卷（即一组题目的组合）。
 * 通过选择、交叉、变异操作不断迭代，使试卷满足预设的约束条件（题型、难度、知识点覆盖等）。
 */
@Service
public class GeneticAlgorithmService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private PaperQuestionRepository paperQuestionRepository;

    @Value("${exam.ga.population-size:50}")
    private int populationSize;

    @Value("${exam.ga.max-generations:200}")
    private int maxGenerations;

    @Value("${exam.ga.crossover-rate:0.8}")
    private double crossoverRate;

    @Value("${exam.ga.mutation-rate:0.1}")
    private double mutationRate;

    @Value("${exam.ga.elite-count:2}")
    private int eliteCount;

    private final Random random = new Random();

    /**
     * 组卷约束条件
     */
    public static class PaperConstraint {
        public String subject;
        public String paperName;
        public int totalScore;
        public int duration;
        public int[] typeCounts = new int[5];
        public int[] difficultyRatio = new int[]{30, 50, 20};

        public static PaperConstraintBuilder builder() {
            return new PaperConstraintBuilder();
        }

        public static class PaperConstraintBuilder {
            private final PaperConstraint constraint = new PaperConstraint();

            public PaperConstraintBuilder subject(String subject) { constraint.subject = subject; return this; }
            public PaperConstraintBuilder paperName(String name) { constraint.paperName = name; return this; }
            public PaperConstraintBuilder totalScore(int score) { constraint.totalScore = score; return this; }
            public PaperConstraintBuilder duration(int duration) { constraint.duration = duration; return this; }
            public PaperConstraintBuilder typeCounts(int singleChoice, int multiChoice, int judge, int fill, int shortAnswer) {
                constraint.typeCounts = new int[]{singleChoice, multiChoice, judge, fill, shortAnswer};
                return this;
            }
            public PaperConstraintBuilder difficultyRatio(int easy, int medium, int hard) {
                constraint.difficultyRatio = new int[]{easy, medium, hard};
                return this;
            }
            public PaperConstraint build() { return constraint; }
        }
    }

    /**
     * 个体（染色体）
     */
    private static class Chromosome {
        List<Long> questionIds;
        double fitness;

        Chromosome(List<Long> questionIds) {
            this.questionIds = new ArrayList<>(questionIds != null ? questionIds : Collections.emptyList());
            this.fitness = 0.0;
        }
    }

    /**
     * 使用遗传算法自动组卷
     */
    @Transactional
    public Paper generatePaper(PaperConstraint constraint) {
        // 参数校验
        if (constraint == null || constraint.subject == null || constraint.subject.trim().isEmpty()) {
            throw new RuntimeException("请选择科目");
        }

        int totalNeeded = 0;
        for (int c : constraint.typeCounts) totalNeeded += c;
        if (totalNeeded == 0) {
            throw new RuntimeException("请至少设置一种题型的数量");
        }

        // 1. 加载题库
        Map<String, List<Question>> questionPool = loadQuestionPool(constraint.subject);
        if (questionPool.isEmpty()) {
            throw new RuntimeException("题库中没有【" + constraint.subject + "】的题目，请先导入题目");
        }

        // 2. 验证题目是否足够
        validateQuestionPool(questionPool, constraint);

        // 3. 初始化种群
        List<Chromosome> population = initializePopulation(questionPool, constraint);

        // 4. 进化迭代
        Chromosome bestChromosome = null;
        for (int generation = 0; generation < maxGenerations; generation++) {
            // 计算适应度
            for (Chromosome c : population) {
                c.fitness = calculateFitness(c, questionPool, constraint);
            }

            // 按适应度排序（降序）
            population.sort((a, b) -> Double.compare(b.fitness, a.fitness));

            // 更新最优个体
            if (bestChromosome == null || population.get(0).fitness > bestChromosome.fitness) {
                bestChromosome = new Chromosome(population.get(0).questionIds);
                bestChromosome.fitness = population.get(0).fitness;
            }

            // 终止条件
            if (population.get(0).fitness >= 0.95) {
                break;
            }

            // 产生新一代
            List<Chromosome> newPopulation = new ArrayList<>();

            // 精英保留
            for (int i = 0; i < eliteCount && i < population.size(); i++) {
                if (population.get(i).questionIds != null && !population.get(i).questionIds.isEmpty()) {
                    newPopulation.add(new Chromosome(population.get(i).questionIds));
                }
            }

            // 补齐到populationSize
            while (newPopulation.size() < populationSize) {
                Chromosome parent1 = tournamentSelect(population);
                Chromosome parent2 = tournamentSelect(population);

                if (parent1.questionIds.isEmpty() || parent2.questionIds.isEmpty()) {
                    // 如果选到空个体，用随机个体替代
                    newPopulation.add(createRandomChromosome(questionPool, constraint));
                    continue;
                }

                Chromosome[] offspring = crossover(parent1, parent2);

                mutate(offspring[0], questionPool);
                mutate(offspring[1], questionPool);

                newPopulation.add(offspring[0]);
                if (newPopulation.size() < populationSize) {
                    newPopulation.add(offspring[1]);
                }
            }

            population = newPopulation;
        }

        // 5. 最终检查：如果最优个体仍为空，直接用随机方式生成
        if (bestChromosome == null || bestChromosome.questionIds.isEmpty()) {
            bestChromosome = createRandomChromosome(questionPool, constraint);
        }

        // 6. 构建试卷
        return buildPaper(bestChromosome, constraint);
    }

    /**
     * 加载题目池
     */
    private Map<String, List<Question>> loadQuestionPool(String subject) {
        List<Question> questions = questionRepository.findBySubject(subject);
        Map<String, List<Question>> pool = new HashMap<>();
        for (Question q : questions) {
            if (q.getType() != null && q.getDifficulty() != null) {
                String key = q.getType() + "_" + q.getDifficulty();
                pool.computeIfAbsent(key, k -> new ArrayList<>()).add(q);
            }
        }
        return pool;
    }

    /**
     * 验证题库
     */
    private void validateQuestionPool(Map<String, List<Question>> pool, PaperConstraint constraint) {
        int[] typeCounts = constraint.typeCounts;
        String[] typeNames = {"单选题", "多选题", "判断题", "填空题", "简答题"};

        for (int type = 1; type <= 5; type++) {
            int need = typeCounts[type - 1];
            if (need == 0) continue;

            int available = 0;
            for (int diff = 1; diff <= 3; diff++) {
                String key = type + "_" + diff;
                List<Question> list = pool.get(key);
                if (list != null) available += list.size();
            }

            if (available < need) {
                throw new RuntimeException("题库中【" + constraint.subject + "】的" + typeNames[type - 1]
                        + "数量不足！需要" + need + "道，仅有" + available + "道");
            }
        }
    }

    /**
     * 初始化种群
     */
    private List<Chromosome> initializePopulation(Map<String, List<Question>> pool, PaperConstraint constraint) {
        List<Chromosome> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(createRandomChromosome(pool, constraint));
        }
        return population;
    }

    /**
     * 创建随机个体
     */
    private Chromosome createRandomChromosome(Map<String, List<Question>> pool, PaperConstraint constraint) {
        List<Long> questionIds = new ArrayList<>();
        int[] typeCounts = constraint.typeCounts;
        int[] ratio = constraint.difficultyRatio;

        for (int type = 1; type <= 5; type++) {
            int need = typeCounts[type - 1];
            if (need <= 0) continue;

            int easyCount = (int) Math.round(need * ratio[0] / 100.0);
            int hardCount = (int) Math.round(need * ratio[2] / 100.0);
            int mediumCount = need - easyCount - hardCount;
            if (mediumCount < 0) mediumCount = 0;

            questionIds.addAll(selectRandomQuestions(pool, type, 1, easyCount));
            questionIds.addAll(selectRandomQuestions(pool, type, 2, mediumCount));
            questionIds.addAll(selectRandomQuestions(pool, type, 3, hardCount));
        }

        Collections.shuffle(questionIds);
        return new Chromosome(questionIds);
    }

    /**
     * 随机选取指定数量的题目
     */
    private List<Long> selectRandomQuestions(Map<String, List<Question>> pool, int type, int difficulty, int count) {
        if (count <= 0) return Collections.emptyList();
        String key = type + "_" + difficulty;
        List<Question> available = pool.get(key);
        if (available == null || available.isEmpty()) return Collections.emptyList();

        List<Question> copy = new ArrayList<>(available);
        Collections.shuffle(copy);
        List<Long> ids = new ArrayList<>();
        int actualCount = Math.min(count, copy.size());
        for (int i = 0; i < actualCount; i++) {
            ids.add(copy.get(i).getId());
        }
        return ids;
    }

    /**
     * 计算适应度
     */
    private double calculateFitness(Chromosome chromosome, Map<String, List<Question>> pool, PaperConstraint constraint) {
        if (chromosome.questionIds == null || chromosome.questionIds.isEmpty()) {
            return 0.0;
        }

        Map<Long, Question> questionMap = getQuestionMap(chromosome.questionIds);
        List<Question> questions = new ArrayList<>();
        for (Long id : chromosome.questionIds) {
            Question q = questionMap.get(id);
            if (q != null) questions.add(q);
        }

        if (questions.isEmpty()) return 0.0;

        double totalScoreFitness = calculateTotalScoreFitness(questions, constraint.totalScore);
        double difficultyFitness = calculateDifficultyFitness(questions, constraint.difficultyRatio);
        double coverageFitness = calculateCoverageFitness(questions);
        double uniquenessFitness = calculateUniquenessFitness(chromosome.questionIds);

        return 0.3 * totalScoreFitness + 0.3 * difficultyFitness
                + 0.2 * coverageFitness + 0.2 * uniquenessFitness;
    }

    private double calculateTotalScoreFitness(List<Question> questions, int targetScore) {
        if (targetScore <= 0) return 1.0;
        int actualScore = 0;
        for (Question q : questions) {
            actualScore += (q.getScore() != null ? q.getScore() : 0);
        }
        double diff = Math.abs(actualScore - targetScore);
        return Math.max(0, 1.0 - diff / (double) targetScore);
    }

    private double calculateDifficultyFitness(List<Question> questions, int[] targetRatio) {
        int total = questions.size();
        if (total == 0) return 0.0;
        int easy = 0, medium = 0, hard = 0;
        for (Question q : questions) {
            int d = q.getDifficulty() != null ? q.getDifficulty() : 2;
            switch (d) {
                case 1: easy++; break;
                case 2: medium++; break;
                case 3: hard++; break;
            }
        }
        double diff = Math.abs(easy * 100.0 / total - targetRatio[0])
                    + Math.abs(medium * 100.0 / total - targetRatio[1])
                    + Math.abs(hard * 100.0 / total - targetRatio[2]);
        return Math.max(0, 1.0 - diff / 200.0);
    }

    private double calculateCoverageFitness(List<Question> questions) {
        Set<String> chapters = new HashSet<>();
        for (Question q : questions) {
            if (q.getChapter() != null && !q.getChapter().isEmpty()) {
                chapters.add(q.getChapter());
            }
        }
        return Math.min(1.0, chapters.size() / 5.0);
    }

    private double calculateUniquenessFitness(List<Long> questionIds) {
        if (questionIds.isEmpty()) return 0.0;
        Set<Long> uniqueIds = new HashSet<>(questionIds);
        return uniqueIds.size() / (double) questionIds.size();
    }

    /**
     * 锦标赛选择
     */
    private Chromosome tournamentSelect(List<Chromosome> population) {
        int tournamentSize = Math.min(3, population.size());
        Chromosome best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Chromosome candidate = population.get(random.nextInt(population.size()));
            if (best == null || candidate.fitness > best.fitness) {
                best = candidate;
            }
        }
        return new Chromosome(best != null ? best.questionIds : Collections.emptyList());
    }

    /**
     * 交叉操作
     */
    private Chromosome[] crossover(Chromosome parent1, Chromosome parent2) {
        Chromosome child1 = new Chromosome(parent1.questionIds);
        Chromosome child2 = new Chromosome(parent2.questionIds);

        int size1 = parent1.questionIds.size();
        int size2 = parent2.questionIds.size();

        if (random.nextDouble() < crossoverRate && size1 > 1 && size2 > 1) {
            int maxSize = Math.min(size1, size2);
            int crossPoint = 1 + random.nextInt(maxSize - 1);

            List<Long> newChild1Ids = new ArrayList<>();
            newChild1Ids.addAll(parent1.questionIds.subList(0, Math.min(crossPoint, size1)));
            if (crossPoint < size2) {
                newChild1Ids.addAll(parent2.questionIds.subList(crossPoint, size2));
            }

            List<Long> newChild2Ids = new ArrayList<>();
            newChild2Ids.addAll(parent2.questionIds.subList(0, Math.min(crossPoint, size2)));
            if (crossPoint < size1) {
                newChild2Ids.addAll(parent1.questionIds.subList(crossPoint, size1));
            }

            child1.questionIds = newChild1Ids;
            child2.questionIds = newChild2Ids;
        }

        return new Chromosome[]{child1, child2};
    }

    /**
     * 变异操作
     */
    private void mutate(Chromosome chromosome, Map<String, List<Question>> pool) {
        if (chromosome.questionIds == null || chromosome.questionIds.isEmpty()) return;

        Map<Long, Question> questionMap = getQuestionMap(chromosome.questionIds);

        for (int i = 0; i < chromosome.questionIds.size(); i++) {
            if (random.nextDouble() < mutationRate) {
                Question oldQ = questionMap.get(chromosome.questionIds.get(i));
                if (oldQ != null && oldQ.getType() != null && oldQ.getDifficulty() != null) {
                    String key = oldQ.getType() + "_" + oldQ.getDifficulty();
                    List<Question> candidates = pool.get(key);
                    if (candidates != null && !candidates.isEmpty()) {
                        Question newQ = candidates.get(random.nextInt(candidates.size()));
                        chromosome.questionIds.set(i, newQ.getId());
                    }
                }
            }
        }
    }

    /**
     * 构建试卷实体
     */
    private Paper buildPaper(Chromosome chromosome, PaperConstraint constraint) {
        Map<Long, Question> questionMap = getQuestionMap(chromosome.questionIds);

        int actualTotalScore = 0;
        double totalDifficulty = 0;
        int validCount = 0;

        for (Long id : chromosome.questionIds) {
            Question q = questionMap.get(id);
            if (q != null) {
                actualTotalScore += (q.getScore() != null ? q.getScore() : 0);
                totalDifficulty += (q.getDifficulty() != null ? q.getDifficulty() : 2);
                validCount++;
            }
        }

        if (validCount == 0) {
            throw new RuntimeException("组卷失败：无法生成有效试卷，请检查题库中题目数量是否充足");
        }

        Paper paper = new Paper();
        paper.setName(constraint.paperName);
        paper.setSubject(constraint.subject);
        paper.setTotalScore(actualTotalScore);
        paper.setDuration(constraint.duration);
        paper.setDifficultyFactor(totalDifficulty / validCount / 3.0);
        paper.setStatus(0);
        paper.setGenerateType(2);
        paper = paperRepository.save(paper);

        // 按题型顺序排列
        int order = 1;
        Set<Long> savedIds = new HashSet<>();
        for (int type = 1; type <= 5; type++) {
            for (Long id : chromosome.questionIds) {
                if (savedIds.contains(id)) continue;
                Question q = questionMap.get(id);
                if (q != null && q.getType() != null && q.getType() == type) {
                    PaperQuestion pq = new PaperQuestion();
                    pq.setPaper(paper);
                    pq.setQuestion(q);
                    pq.setQuestionOrder(order++);
                    pq.setScore(q.getScore() != null ? q.getScore() : 2);
                    paperQuestionRepository.save(pq);
                    savedIds.add(id);
                }
            }
        }

        return paper;
    }

    /**
     * 批量获取题目
     */
    private Map<Long, Question> getQuestionMap(List<Long> ids) {
        Map<Long, Question> map = new HashMap<>();
        if (ids == null) return map;
        for (Long id : ids) {
            if (id != null) {
                questionRepository.findById(id).ifPresent(q -> map.put(id, q));
            }
        }
        return map;
    }
}
