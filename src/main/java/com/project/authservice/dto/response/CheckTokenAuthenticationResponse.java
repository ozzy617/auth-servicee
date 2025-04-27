package com.project.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class CheckTokenAuthenticationResponse {
    private UserDetails userDetails;
    private boolean isAuthenticated;

}
