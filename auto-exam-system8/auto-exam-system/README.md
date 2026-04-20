# 基于Spring Boot的自动组卷系统

## 📌 项目简介

本系统是一个基于 **Spring Boot + 遗传算法** 的智能自动组卷平台。管理员可以通过Web界面管理题库，并利用遗传算法（Genetic Algorithm）自动生成符合约束条件的试卷。

本项目适用于**计算机相关专业本科毕业设计**，涵盖了后端开发、算法设计、数据库设计、前端页面等核心知识点。

## 🔧 技术栈

| 类别 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.18 |
| 安全框架 | Spring Security |
| 持久层 | Spring Data JPA + Hibernate |
| 数据库 | H2（默认）/ MySQL（生产） |
| 模板引擎 | Thymeleaf |
| 前端 | HTML5 + CSS3（原生） |
| 构建工具 | Maven |
| JDK版本 | 1.8+ |

## 🧬 遗传算法设计

### 核心思想
将组卷问题建模为**组合优化问题**：
- **染色体**：一套试卷 = 一组题目ID的有序组合
- **基因**：每道题目
- **适应度函数**：综合评分，衡量试卷质量

### 适应度函数（加权评分）

| 评估维度 | 权重 | 说明 |
|----------|------|------|
| 总分吻合度 | 30% | 实际总分与目标总分的接近程度 |
| 难度分布吻合度 | 30% | 实际难度比例与目标比例的偏差 |
| 知识点覆盖度 | 20% | 试卷涵盖的章节/知识点是否广泛 |
| 题目唯一性 | 20% | 避免同一题目重复出现在试卷中 |

### 算法流程
1. **初始化种群**：根据约束随机生成 N 套候选试卷
2. **适应度评估**：对每套试卷计算适应度得分
3. **选择操作**：锦标赛选择（Tournament Selection）
4. **交叉操作**：单点交叉，交换部分题目
5. **变异操作**：随机替换某道同类型同难度的题目
6. **精英保留**：保留每代最优的个体
7. **迭代终止**：达到最大迭代次数或适应度≥0.95

## 📁 项目结构

```
auto-exam-system/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/exam/
    │   ├── AutoExamApplication.java          # 启动类
    │   ├── config/
    │   │   ├── SecurityConfig.java           # Spring Security配置
    │   │   └── DataInitializer.java          # 数据初始化（默认管理员）
    │   ├── entity/
    │   │   ├── Admin.java                    # 管理员实体
    │   │   ├── Question.java                 # 试题实体
    │   │   ├── Paper.java                    # 试卷实体
    │   │   └── PaperQuestion.java            # 试卷-试题关联
    │   ├── repository/
    │   │   ├── AdminRepository.java
    │   │   ├── QuestionRepository.java
    │   │   ├── PaperRepository.java
    │   │   └── PaperQuestionRepository.java
    │   ├── service/
    │   │   ├── AdminDetailsService.java      # 用户认证服务
    │   │   ├── QuestionService.java          # 题库业务逻辑
    │   │   ├── PaperService.java             # 试卷业务逻辑
    │   │   └── GeneticAlgorithmService.java  # ⭐ 遗传算法核心
    │   └── controller/
    │       ├── PageController.java           # 页面跳转
    │       ├── QuestionController.java       # 题库管理
    │       ├── PaperController.java          # 试卷管理
    │       └── ApiController.java            # REST API
    └── resources/
        ├── application.yml                   # 配置文件
        ├── static/css/style.css              # 样式
        └── templates/
            ├── login.html                    # 登录页
            ├── dashboard.html                # 控制台
            ├── question/
            │   ├── list.html                 # 题库列表
            │   └── form.html                 # 题目编辑
            └── paper/
                ├── list.html                 # 试卷列表
                ├── generate.html             # 自动组卷
                └── view.html                 # 试卷详情
```

## 🚀 运行指南

### 环境要求
- JDK 1.8 或以上
- Maven 3.6+

### 快速启动

```bash
# 1. 进入项目目录
cd auto-exam-system

# 2. 编译打包
mvn clean package -DskipTests

# 3. 运行
java -jar target/auto-exam-system-1.0.0.jar
```

或直接用 Maven 运行：

```bash
mvn spring-boot:run
```

### 访问系统
- 地址：http://localhost:8080
- 用户名：`admin`
- 密码：`admin123`

### 使用流程
1. 登录系统
2. 点击「📥 导入示例题」导入预设的题目数据
3. 进入「🧬 自动组卷」，设置试卷参数
4. 点击「开始智能组卷」，系统自动生成试卷
5. 查看试卷详情，可发布或删除

## 📊 数据库设计

### ER关系
```
Admin (管理员)
    │
Question (试题) ──────┐
    │                  │
    └── Paper (试卷) ←── PaperQuestion (关联表)
```

### 核心表

**admin** - 管理员表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR | 密码（BCrypt加密） |
| real_name | VARCHAR(50) | 真实姓名 |
| role | INT | 角色 0-超级管理员 1-普通管理员 |

**question** - 试题表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| type | INT | 题型 1-单选 2-多选 3-判断 4-填空 5-简答 |
| difficulty | INT | 难度 1-简单 2-中等 3-困难 |
| subject | VARCHAR(100) | 所属科目 |
| chapter | VARCHAR(200) | 章节/知识点 |
| content | TEXT | 题目内容 |
| options | TEXT | 选项（JSON） |
| answer | TEXT | 正确答案 |
| analysis | TEXT | 解析 |
| score | INT | 默认分值 |

**paper** - 试卷表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| name | VARCHAR(200) | 试卷名称 |
| subject | VARCHAR(100) | 所属科目 |
| total_score | INT | 总分 |
| duration | INT | 考试时长（分钟） |
| difficulty_factor | DOUBLE | 难度系数 |
| status | INT | 状态 0-草稿 1-已发布 |
| generate_type | INT | 组卷方式 1-手动 2-自动 |

## 🎯 功能模块

- [x] 管理员登录/退出（Spring Security）
- [x] 题库管理（增删改查、搜索、分页）
- [x] 示例题库一键导入（高等数学、大学英语、计算机网络、数据结构）
- [x] 自动组卷（遗传算法）
- [x] 试卷管理（查看、发布、删除）
- [x] 统计数据展示

## 📝 注意事项

- 默认使用 **H2 内存数据库**，重启后数据会清空
- 如需持久化数据，请修改 `application.yml` 中的数据库配置为 MySQL
- 遗传算法的参数（种群大小、迭代次数等）可在 `application.yml` 中调整

## 📜 许可

本项目仅供学习和毕业设计参考使用。
