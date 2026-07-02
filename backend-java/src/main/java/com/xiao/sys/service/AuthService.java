package com.xiao.sys.service;

import com.xiao.sys.dto.ChangePasswordDTO;
import com.xiao.sys.dto.LoginDTO;
import com.xiao.sys.dto.UserInfoDTO;

import java.util.Map;

public interface AuthService {

    Map<String, Object> login(LoginDTO dto);

    void logout();

    UserInfoDTO getCurrentUserInfo();

    void changePassword(ChangePasswordDTO dto);
}
