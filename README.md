# AI Java Interviewer

一个基于 AI 的 Java 面试/练习助手。它利用 Spring AI 和大语言模型（LLM）为用户提供模拟面试、简历分析和知识点复习等功能。


## 🌟 核心功能

*   **智能模拟面试**：基于 RAG (检索增强生成) 技术，根据 Java 知识库进行深度问答。
*   **简历分析**：上传简历 (PDF/Markdown)，AI 自动提取关键技能并生成针对性的面试题。
*   **错题本**：记录面试中的薄弱环节，支持复习和重练。
*   **知识点管理**：系统化追踪已掌握和未掌握的技术点。
*   **安全认证**：基于 JWT 的用户认证系统。
*   **限流保护**：内置 Redis 限流机制，防止滥用。

## 🛠 技术栈

### 后端 (Backend)
*   **Java**: 21
*   **Framework**: Spring Boot 3.4.1
*   **ORM**: MyBatis-Plus 3.5.14
*   **AI**: Spring AI Alibaba (集成 DashScope/通义千问)
*   **Database**: PostgreSQL (支持 pgvector 插件)
*   **Cache**: Redis
*   **Utils**: Hutool


## 🚀 快速开始

### 1. 环境准备

*   JDK 21+
*   Node.js 20+
*   PostgreSQL 15+ (必须安装 `vector` 插件)
*   Redis 

### 2. 数据库初始化

1.  创建一个名为 `aiinterview` 的 PostgreSQL 数据库。
2.  在数据库中执行根目录下的 SQL 脚本，建议顺序如下：
    *   `users.sql` (用户表)
    *   `resumes.sql` (简历表)
    *   `interview_chat.sql` (面试对话表)
    *   `mistake_book.sql` (错题本表)
    *   `vector_store.sql` (向量存储表)

### 3. 后端配置与启动

1.  修改 `src/main/resources/application.yml`：
    *   配置 PostgreSQL 连接信息 (`spring.datasource`).
    *   配置 Redis 连接信息 (`spring.data.redis`).
    *   **重要**：配置阿里云 DashScope API Key。你可以在配置文件中添加 `spring.ai.dashscope.api-key` 或者设置环境变量 `SPRING_AI_DASHSCOPE_API_KEY`。
2.  启动应用：
    ```bash
    ./mvnw spring-boot:run
    ```
    或者在 IDE 中运行 `AiJavaInterviewerApplication` 类。
    后端默认运行在端口 `8123`。
    API 文档地址: http://localhost:8123/api/doc.html


## 📂 项目结构

```
ai-java-interviewer/
├── ai-java-interviewer-front/   # 前端项目 (Vue 3 + TS)
├── src/main/java/com/muyulu/aijavainterviewer/ # 后端源码根目录
│   ├── AiJavaInterviewerApplication.java # Spring Boot 启动类
│   ├── aspect/                # AOP 切面 (如 RateLimitAspect 限流)
│   ├── assistant/             # Spring AI Assistant 定义 (InterviewAssistant, ResumeAgent)
│   ├── common/                # 通用模块
│   ├── config/                # 配置类 (AI, Redis, Security, WebMvc 等)
│   ├── controller/            # Web 层控制器 (API 接口)
│   │   ├── InterviewChatController.java    # 面试对话：发送消息、重置上下文
│   │   ├── InterviewTopicsController.java  # 题库管理：获取知识点、题目生成
│   │   ├── MistakeBookController.java      # 错题本：查询、添加错题
│   │   ├── ResumeController.java           # 简历管理：上传、解析、分析
│   │   └── UserController.java             # 用户管理：注册、登录、信息查询
│   ├── filter/                # 过滤器 (JwtAuthenticationFilter)
│   ├── graph/                 # 状态图定义 (用于复杂面试流程流转)
│   ├── interceptor/           # 拦截器 (登录检查)
│   ├── mapper/                # 数据访问层 (MyBatis Mapper)
│   ├── model/                 # 数据模型
│   │   ├── dto/               # 数据传输对象 (DTO)
│   │   ├── entity/            # 数据库实体 (Entity)
│   │   ├── enums/             # 枚举类型
│   │   └── vo/                # 视图对象 (VO)
│   ├── service/               # 业务逻辑接口
│   │   ├── InterviewChatService.java       # 面试对话服务：处理用户输入、调用 AI
│   │   ├── InterviewGraphService.java      # 流程编排服务：基于 Graph 生成定制化题库
│   │   ├── MistakeBookService.java         # 错题本服务：错题的增删改查
│   │   ├── RagService.java                 # RAG 服务：向量库检索、关键词提取
│   │   ├── ResumeService.java              # 简历服务：简历解析、存储与分析
│   │   ├── UserService.java                # 用户服务：用户认证与管理
│   │   └── impl/                           # 业务逻辑实现类
│   └── tool/                  # AI Function Calling 工具类
├── src/main/resources/        # 资源文件
│   ├── mapper/                # MyBatis XML 映射文件
│   ├── application.yml        # 主配置文件
│   └── application-prod.yml   # 生产环境配置
├── *.sql                      # 数据库初始化脚本
└── pom.xml                    # Maven 依赖配置
```
