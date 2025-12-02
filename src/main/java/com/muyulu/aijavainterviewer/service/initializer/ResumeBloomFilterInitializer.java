package com.muyulu.aijavainterviewer.service.initializer;

import com.google.common.hash.BloomFilter;
import com.muyulu.aijavainterviewer.mapper.ResumeMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeBloomFilterInitializer {

    private final ResumeMapper resumeMapper;
    private final BloomFilter<String> resumeBloomFilter;

    @PostConstruct
    public void initBloomFilter() {
        log.info("Initializing resume Bloom filter");
        resumeMapper.selectList(null).forEach(resume -> {
            if (resume.getResumeId() != null) {
                resumeBloomFilter.put(resume.getResumeId());
            }
        });
        log.info("Resume Bloom filter initialized");
    }
}

