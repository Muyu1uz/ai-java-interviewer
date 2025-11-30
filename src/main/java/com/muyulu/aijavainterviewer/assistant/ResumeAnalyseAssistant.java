package com.muyulu.aijavainterviewer.assistant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class ResumeAnalyseAssistant {

    private final ChatClient chatClient;

    public ResumeAnalyseAssistant(ChatModel chatModel){
        chatClient = ChatClient.create(chatModel);
    }


    private String analyzeWithAI(String resumeContent) {
        String analysisPrompt = """
            请分析以下简历内容，提取结构化信息：

            要求：
            1. 基本信息：姓名、联系方式、求职意向
            2. 教育背景：学校、专业、学历、时间
            3. 专业技能：编程语言、框架、工具等（按熟练度分类）
            4. 实习/工作经历：公司、职位、时间、主要工作内容
            5. 项目经历：项目名称、技术栈、角色、时间、项目描述
            6. 识别核心技术栈和可提问的技术点

            请以JSON格式返回结构化数据。

            简历内容：
            """ + resumeContent;

        return chatClient.prompt()
                .system("你是专业的简历分析专家，擅长提取和结构化简历信息")
                .user(analysisPrompt)
                .call()
                .content();
    }

}
