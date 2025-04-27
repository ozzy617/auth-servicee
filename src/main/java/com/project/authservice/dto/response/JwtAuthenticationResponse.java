package com.project.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtAuthenticationResponse {
 //   private String token;
    private String token;
    private String refreshToken;
}
