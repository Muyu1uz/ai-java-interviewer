# AI Java Interviewer

**一个用于 Java 面试/练习、基于 AI 的面试问答的项目。**

**项目亮点**
- **AI 驱动**: 集成 Spring AI 组件，为候选人/学习者提供交互式面试问答体验。
- **面试知识点管理**: 支持用户记录、统计与重置已问过的技术点，便于系统化复习。
- **JWT 验证**: 内置简单的 JWT 认证机制用于接口保护。
- **技术栈**: Spring Boot + MyBatis-Plus + Redis+ Spring AI。

# 开始页面
<img width="2427" height="1139" alt="image" src="https://github.com/user-attachments/assets/f8015a63-f7e3-4eb8-841c-fd307b093690" />

# 登陆页面
<img width="1140" height="1023" alt="image" src="https://github.com/user-attachments/assets/b2a923d0-4626-4273-9966-86b8e0ab8b86" />

# 简历上传与分析
<img width="2166" height="1119" alt="image" src="https://github.com/user-attachments/assets/9a5cff4a-79ae-4f9d-8c31-3afcbf2a96ad" />
<img width="1622" height="1060" alt="image" src="https://github.com/user-attachments/assets/32935cdb-2976-4a9e-8f67-e092c0148900" />

# 对话页面
<img width="2559" height="1339" alt="image" src="https://github.com/user-attachments/assets/adc9ed89-499d-4542-824a-0b161f4845ea" />
<img width="2463" height="1196" alt="image" src="https://github.com/user-attachments/assets/672f5590-11a8-4a7c-bc11-c6989bc2b29e" />

**环境与依赖**
- JDK 21
- Maven 3.8+ 或使用仓库中的 `mvnw`（推荐）
- PostgreSQL（见 `application.yml` 配置）
- Redis（用于缓存或限流，需在 `application.yml` 中开启并配置）

# 快速开始
5分钟开启你的第一次AI面试：

> **1. 克隆仓库**
> git clone https://github.com/Muyu1uz/ai-java-interviewer.git
>
> **2. 启动服务**
> 1.JDK17+
> 2.使用的数据库postgreSql，但是需要注意使用带有支持向量库的postgreSql。数据库的DDL和数据已经上传
> 3.申请阿里云百炼API Key
> 4.配置好自己的Redis
> 
> **3. 访问API文档**
> open http://localhost:8123/api/doc.html#/home
