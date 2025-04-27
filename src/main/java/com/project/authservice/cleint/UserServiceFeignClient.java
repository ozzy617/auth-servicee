package com.project.authservice.cleint;

import com.project.authservice.cleint.dto.request.StartInfoUserRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-feign-client",
        url = "${module.auth.service-url}"
)
public interface UserServiceFeignClient {

    @PostMapping("/api/user/create")
    ResponseEntity<Void> createStartUserInfo(@RequestBody StartInfoUserRequest userInfoRequest);
}
