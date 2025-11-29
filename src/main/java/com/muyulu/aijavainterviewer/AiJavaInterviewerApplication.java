package com.muyulu.aijavainterviewer;

import com.muyulu.aijavainterviewer.config.PgVectorVectorStoreConfig;
import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
public class AiJavaInterviewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiJavaInterviewerApplication.class, args);
    }

}
