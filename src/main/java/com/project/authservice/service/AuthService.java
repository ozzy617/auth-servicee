package com.project.authservice.service;

import com.project.authservice.cleint.UserServiceFeignClient;
import com.project.authservice.cleint.dto.request.StartInfoUserRequest;
import com.project.authservice.dto.request.SignInRequest;
import com.project.authservice.dto.request.SignUpRequest;
import com.project.authservice.dto.response.JwtAuthenticationResponse;
import com.project.authservice.entity.Role;
import com.project.authservice.entity.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
   // private final UserServiceFeignClient userServiceFeignClient;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserServiceFeignClient userServiceFeignClient;
   // private final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    /**
     * Регистрация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    @Transactional
    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID()); // Генерация уникального UUID
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.ROLE_USER);

        userService.create(user);
        //logger.info("Сохранена сущность пользователя UsersEntity");

//        UserInfoRequest userInfo = new UserInfoRequest();
//        userInfo.setUserId(user.getId());
//        userInfo.setUsername(user.getUsername());
//        userInfo.setEmail(user.getEmail());

        //userServiceFeignClient.createUserInfo(userInfo);
        //logger.info("Успешно отправлен запрос на создание информации по пользователю. Запрос /api/user/info, request = " + userInfo.toJson());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        StartInfoUserRequest startInfoUserRequest = new StartInfoUserRequest(user.getUsername(), user.getId());
        userServiceFeignClient.createStartUserInfo(startInfoUserRequest);

        // logger.info("Сгенерирован jwt токен по сущности пользователя UsersEntity");

        return new JwtAuthenticationResponse(token, refreshToken);
    }

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signIn(SignInRequest request) {
         authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));
        //SecurityContextHolder.getContext().setAuthentication(authentication);
        //UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(request.getUsername());
        String jwt = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new JwtAuthenticationResponse(jwt, refreshToken);
//        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//                request.getUsername(),
//                request.getPassword()
//        ));
//
//        //logger.info("Произведена аутентификация пользователя");
//
//        UserDetails user = userService.userDetailsService().loadUserByUsername(request.getUsername());
//        //logger.info("Получен UserDetails пользователя по username = " + request.getUsername());
//
//        String jwt = jwtService.generateToken(user);
//       // logger.info("Сгенерирован jwt токен по сущности пользователя UsersEntity");
//
//        return new JwtAuthenticationResponse(jwt, null);
    }
}
