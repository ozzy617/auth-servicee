package com.project.authservice.cleint.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class StartInfoUserRequest {
    private String username;
    private UUID userId;
}
