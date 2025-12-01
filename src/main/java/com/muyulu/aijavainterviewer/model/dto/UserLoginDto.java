package com.muyulu.aijavainterviewer.model.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginDto(@NotBlank String userAccount,
                           @NotBlank String password)
{}
