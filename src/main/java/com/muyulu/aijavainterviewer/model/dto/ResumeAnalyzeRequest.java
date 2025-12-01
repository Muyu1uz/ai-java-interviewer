package com.muyulu.aijavainterviewer.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ResumeAnalyzeRequest(
        @NotBlank(message = "resumeContent不能为空") String resumeContent
) {}

