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
*   **Cache**: Redis、Redission
*   **Utils**: Hutool


## 🤖 AI 助手介绍

本项目集成了两个核心 AI 助手，分别负责面试交互和简历处理：

### 1. InterViewAssistant (面试官助手)
*   **角色**: 充当一位经验丰富的 Java 技术面试官。
*   **核心能力**:
    *   **上下文记忆**: 基于 `MessageWindowChatMemory` 维护最近 50 条对话记录，确保面试过程的连贯性，能追问和引用之前的回答。
    *   **RAG 增强**: 结合 `RagService`，在生成问题或评价时检索本地向量数据库中的 Java 知识点，确保内容的准确性和专业深度。
    *   **智能交互**: 使用 `ChatClient` 进行流式对话，提供低延迟的实时交互体验。

### 2. ResumeAgent (简历分析专家)
*   **角色**: 专业的简历分析与结构化提取专家。
*   **核心能力**:
    *   **ReAct 模式**: 基于 Spring AI Alibaba 的 `ReactAgent` 构建，具备更强的推理和任务执行能力。
    *   **结构化提取**: 能够从非结构化的简历文本（PDF/Markdown 解析后的文本）中，精确提取出“专业知识”、“项目经验”、“实习经验”等关键信息。
    *   **JSON 输出**: 严格遵循 JSON Schema 输出标准化的数据，便于后续系统根据简历内容生成定制化的面试题库。

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

### 4. 系统界面
<img width="2520" height="1334" alt="image" src="https://github.com/user-attachments/assets/f41b391d-4132-4fc0-8e88-95c951ad6661" />
<img width="2546" height="1311" alt="image" src="https://github.com/user-attachments/assets/51727e45-54b6-469c-9c72-8e4f7b07ccb2" />
<img width="2479" height="1245" alt="image" src="https://github.com/user-attachments/assets/24d9389b-2cbb-4e62-a63e-6dd53c953a2e" />
<img width="2513" height="1317" alt="image" src="https://github.com/user-attachments/assets/5adb7dd1-8762-48e9-a844-60591d6e7ffe" />
<img width="2520" height="1318" alt="image" src="https://github.com/user-attachments/assets/0b3e97aa-5a05-4f3d-9502-37c7202d365f" />
<img width="2511" height="1332" alt="image" src="https://github.com/user-attachments/assets/3f39c665-9e46-41f6-81c4-9aa887300604" />
<img width="2491" height="1302" alt="image" src="https://github.com/user-attachments/assets/fa0b1bd2-5ce3-4d04-952b-010f0f44184b" />


## 🤖 AI 助手介绍

本项目集成了两个核心 AI 助手，分别负责面试交互和简历处理：

### 1. InterViewAssistant (面试官助手)
*   **角色**: 充当一位经验丰富的 Java 技术面试官。
*   **核心能力**:
    *   **上下文记忆**: 基于 `MessageWindowChatMemory` 维护最近 50 条对话记录，确保面试过程的连贯性，能追问和引用之前的回答。
    *   **RAG 增强**: 结合 `RagService`，在生成问题或评价时检索本地向量数据库中的 Java 知识点，确保内容的准确性和专业深度。
    *   **智能交互**: 使用 `ChatClient` 进行流式对话，提供低延迟的实时交互体验。

### 2. ResumeAgent (简历分析专家)
*   **角色**: 专业的简历分析与结构化提取专家。
*   **核心能力**:
    *   **ReAct 模式**: 基于 Spring AI Alibaba 的 `ReactAgent` 构建，具备更强的推理和任务执行能力。
    *   **结构化提取**: 能够从非结构化的简历文本（PDF/Markdown 解析后的文本）中，精确提取出“专业知识”、“项目经验”、“实习经验”等关键信息。
    *   **JSON 输出**: 严格遵循 JSON Schema 输出标准化的数据，便于后续系统根据简历内容生成定制化的面试题库。

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
