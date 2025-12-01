package com.muyulu.aijavainterviewer.assistant;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.muyulu.aijavainterviewer.model.vo.ResumeVo;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ResumeAgent {

    private final ReactAgent reactAgent;
    private String analysisSchema = """
        {
          "$schema": "https://json-schema.org/draft/2020-12/schema",
          "type": "object",
          "properties": {
            "professionalKnowledge": {
              "type": "string",
              "description": "专业知识"
            },
            "projectExperience": {
              "type": "string",
              "description": "项目经验"
            },
            "internshipExperience": {
              "type": "string",
              "description": "实习经验"
            },
            "createTime": {
              "type": "string",
              "format": "date-time",
              "description": "创建时间"
            },
            "updateTime": {
              "type": "string",
              "format": "date-time",
              "description": "更新时间"
            }
          },
          "additionalProperties": false
}""";


    public ResumeAgent(@Qualifier("dashScopeChatModel") ChatModel chatModel) {
        this.reactAgent = ReactAgent.builder()
                .name("ResumeAgent")
                .model(chatModel)
                .systemPrompt("你是专业的简历分析专家，擅长提取和结构化简历信息。你输出的JSON结构必须是纯净JSON内容，以'{'开头，以'}'结尾，不能包含任何多余的文本描述。")
                .outputSchema(analysisSchema)
                .build();
    }

    public String analyzeResume(String resumeContent) throws GraphRunnerException {
        AssistantMessage call = reactAgent.call(resumeContent);
        return call.getText();
    }
}
