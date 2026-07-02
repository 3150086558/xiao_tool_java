package com.xiao.sys.controller;

import com.xiao.sys.common.Result;
import com.xiao.sys.security.LoginUser;
import com.xiao.sys.service.SysDataScopeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sys/data-scope")
public class DataScopeController {

    private final SysDataScopeService sysDataScopeService;

    public DataScopeController(SysDataScopeService sysDataScopeService) {
        this.sysDataScopeService = sysDataScopeService;
    }

    @GetMapping("/visible-users")
    public Result<List<Integer>> getVisibleUsers() {
        Integer userId = getCurrentUserId();
        return Result.success(sysDataScopeService.getVisibleUserIds(userId));
    }

    @GetMapping("/visible-orgs")
    public Result<List<Integer>> getVisibleOrgs() {
        Integer userId = getCurrentUserId();
        return Result.success(sysDataScopeService.getVisibleOrgIds(userId));
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}
