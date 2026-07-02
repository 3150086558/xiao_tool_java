package com.xiao.sys.controller;

import com.xiao.sys.common.Result;
import com.xiao.sys.dto.ChangePasswordDTO;
import com.xiao.sys.dto.LoginDTO;
import com.xiao.sys.dto.UserInfoDTO;
import com.xiao.sys.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sys")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        Map<String, Object> result = authService.login(dto);
        return Result.success(result);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @GetMapping("/userinfo")
    public Result<UserInfoDTO> userInfo() {
        UserInfoDTO info = authService.getCurrentUserInfo();
        return Result.success(info);
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        authService.changePassword(dto);
        return Result.success();
    }
}
