package com.muyulu.aijavainterviewer;

import com.muyulu.aijavainterviewer.config.PgVectorVectorStoreConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
@MapperScan("com.muyulu.aijavainterviewer.mapper")
public class AiJavaInterviewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiJavaInterviewerApplication.class, args);
    }

}
