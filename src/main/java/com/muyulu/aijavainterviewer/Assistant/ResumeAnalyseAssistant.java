package com.muyulu.aijavainterviewer.Assistant;

import com.muyulu.aijavainterviewer.model.entity.InterviewQuestion;
import com.muyulu.aijavainterviewer.model.entity.ResumeAnalysis;
import com.muyulu.aijavainterviewer.model.entity.ResumeStructuredInfo;
import com.muyulu.aijavainterviewer.model.enums.AnalysisStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
public class ResumeAnalyseAssistant {
//    private final ResumeAnalysisRepository resumeRepository;
//    private final ChatClient chatClient;
//    private final FileStorageService fileStorageService;
//
//    public ResumeAnalysisResult analyzeResume(String chatId, MultipartFile file) {
//        try {
//            // 1. 保存文件上传记录
//            ResumeAnalysis resume = saveUploadRecord(chatId, file);
//
//            // 2. 解析文件内容
//            String fileContent = extractFileContent(file);
//
//            // 3. AI分析简历
//            String analysisResult = analyzeWithAI(fileContent);
//
//            // 4. 结构化存储分析结果
//            ResumeStructuredInfo structuredInfo = parseAndStore(resume, analysisResult);
//
//            // 5. 生成面试问题池
//            List<InterviewQuestion> questions = generateInterviewQuestions(resume, structuredInfo);
//
//            // 6. 更新状态
//            resume.setStatus(AnalysisStatus. COMPLETED);
//            resume.setAnalysisTime(LocalDateTime.now());
//            resumeRepository.save(resume);
//
//            return new ResumeAnalysisResult(resume, structuredInfo, questions);
//
//        } catch (Exception e) {
//            log. error("简历分析失败: chatId={}", chatId, e);
//            updateAnalysisStatus(chatId, AnalysisStatus.FAILED);
//            throw new ResumeAnalysisException("简历分析失败", e);
//        }
//    }
//
//    private String analyzeWithAI(String resumeContent) {
//        String analysisPrompt = """
//            请分析以下简历内容，提取结构化信息：
//
//            要求：
//            1.  基本信息：姓名、联系方式、求职意向
//            2.  教育背景：学校、专业、学历、时间
//            3.  专业技能：编程语言、框架、工具等（按熟练度分类）
//            4.  实习/工作经历：公司、职位、时间、主要工作内容
//            5. 项目经历：项目名称、技术栈、角色、时间、项目描述
//            6. 识别核心技术栈和可提问的技术点
//
//            请以JSON格式返回结构化数据。
//
//            简历内容：
//            """ + resumeContent;
//
//        return chatClient.prompt()
//                .system("你是专业的简历分析专家，擅长提取和结构化简历信息")
//                . user(analysisPrompt)
//                .call()
//                .content();
//    }
//
//    private ResumeStructuredInfo parseAndStore(ResumeAnalysis resume, String analysisResult) {
//        try {
//            // 解析AI返回的JSON结构
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String, Object> analysisData = mapper.readValue(analysisResult, Map.class);
//
//            ResumeStructuredInfo structuredInfo = new ResumeStructuredInfo();
//            structuredInfo.setResumeAnalysis(resume);
//            structuredInfo. setRawAnalysisText(analysisResult);
//
//            // 提取各部分信息
//            structuredInfo.setBasicInfo((Map<String, Object>) analysisData.get("basicInfo"));
//            structuredInfo. setEducationInfo((Map<String, Object>) analysisData. get("educationInfo"));
//            structuredInfo.setSkills((List<Map<String, Object>>) analysisData.get("skills"));
//            structuredInfo.setWorkExperience((List<Map<String, Object>>) analysisData.get("workExperience"));
//            structuredInfo.setProjectExperience((List<Map<String, Object>>) analysisData. get("projectExperience"));
//
//            return structuredInfo;
//
//        } catch (Exception e) {
//            log.error("解析AI分析结果失败", e);
//            // 如果JSON解析失败，至少保存原始文本
//            ResumeStructuredInfo structuredInfo = new ResumeStructuredInfo();
//            structuredInfo.setResumeAnalysis(resume);
//            structuredInfo.setRawAnalysisText(analysisResult);
//            return structuredInfo;
//        }
//    }
}
