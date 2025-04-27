package com.project.authservice.dto;

import com.project.authservice.dto.request.SignInRequest;
import com.project.authservice.dto.request.SignUpRequest;
import com.project.authservice.dto.response.CheckTokenAuthenticationResponse;
import com.project.authservice.dto.response.JwtAuthenticationResponse;
import com.project.authservice.dto.request.RefreshTokenRequest;
import com.project.authservice.service.AuthService;
import com.project.authservice.service.JwtService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authenticationService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    //private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/sign-up")
    public JwtAuthenticationResponse signUp(@RequestBody SignUpRequest request) {
        //logger.info("Получен запрос на регистрацию пользователя (POST /auth/sign-up), request = {}", request.toJson());
        JwtAuthenticationResponse response = authenticationService.signUp(request);
       // logger.info("Успешно обработан запрос (POST /auth/sign-up)");
        return response;
    }

    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody SignInRequest request) {
       // logger.info("Получен запрос на аутентификацию пользователя (POST /auth/sign-in), request = {}", request.toJson());
        JwtAuthenticationResponse response = authenticationService.signIn(request);
      //  logger.info("Успешно обработан запрос (POST /auth/sign-in)");
        return response;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody SignInRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        if (jwtService.isRefreshTokenValid(requestRefreshToken)) {
            UserDetails userDetails = jwtService.extractUserDetails(requestRefreshToken);
            String token = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(new JwtAuthenticationResponse(token, requestRefreshToken));
        } else {
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }
    }

    @PostMapping("/token/validate")
    @RateLimiter(name = "jwtRateLimiter") //fallbackMethod = "rateLimiterFallback"
    @Retry(name = "jwtRetry", fallbackMethod = "retryFallback")
    public CheckTokenAuthenticationResponse isTokenValid(@RequestBody String token) {
       // logger.info("Получен запрос на проверку валидности токена пользователя (POST /auth/token/validate), request = {}", token);
        CheckTokenAuthenticationResponse response = jwtService.validateToken(token);
      //  logger.info("Успешно обработан запрос (POST /auth/token/validate)");
        return response;
    }

    public CheckTokenAuthenticationResponse retryFallback(String token, Throwable throwable) {
        // Логика fallback
        System.out.println("RETRY FALLBACK: ");
        return new CheckTokenAuthenticationResponse();
    }
}
