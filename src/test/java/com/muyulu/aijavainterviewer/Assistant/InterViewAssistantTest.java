package com.muyulu.aijavainterviewer.Assistant;

import com.github.xiaoymin.knife4j.core.util.Assert;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InterViewAssistantTest {

    @Resource
    private InterViewAssistant interViewAssistant;

    @Test
    void chat() {
        String chat = interViewAssistant.chat("1", "你好，我是JUC高手");
        Assertions.assertNotNull(chat);
    }
}