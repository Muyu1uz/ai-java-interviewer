package com.muyulu.aijavainterviewer.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class BloomFilterConfig {

    @Bean
    public BloomFilter<String> resumeBloomFilter(
            @Value("${resume.bloom.expectedInsertions:10000}") long expectedInsertions,
            @Value("${resume.bloom.fpp:0.01}") double falsePositiveProbability) {
        return BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                expectedInsertions,
                falsePositiveProbability
        );
    }
}

