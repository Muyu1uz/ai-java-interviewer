package com.muyulu.aijavainterviewer.model.dto;

import jakarta.validation.constraints.NotBlank;

public record UserDto(
        @NotBlank String username,
        @NotBlank String userAccount,
        @NotBlank String password
) {}

