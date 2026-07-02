package com.xiao.sys.controller;

import com.xiao.sys.common.Result;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.dto.UserDTO;
import com.xiao.sys.entity.SysUser;
import com.xiao.sys.service.SysUserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys/user")
public class UserController {

    private final SysUserService sysUserService;

    public UserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<PageResult<UserDTO>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer orgId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        UserDTO query = new UserDTO();
        query.setKeyword(keyword);
        query.setOrgId(orgId);
        query.setStatus(status);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        return Result.success(sysUserService.getUserPage(query));
    }

    @GetMapping("/page")
    public Result<PageResult<UserDTO>> getUserPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer orgId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        UserDTO query = new UserDTO();
        String keyword = null;
        if (name != null && !name.isEmpty()) {
            keyword = name;
        } else if (username != null && !username.isEmpty()) {
            keyword = username;
        }
        query.setKeyword(keyword);
        query.setOrgId(orgId);
        query.setStatus(status);
        query.setPageNum(page);
        query.setPageSize(size);
        return Result.success(sysUserService.getUserPage(query));
    }

    @PostMapping
    public Result<SysUser> createUser(@RequestBody UserDTO dto) {
        return Result.success(sysUserService.createUser(dto));
    }

    @PutMapping("/{id}")
    public Result<SysUser> updateUser(@PathVariable Integer id, @RequestBody UserDTO dto) {
        return Result.success(sysUserService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Integer id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }

    @PutMapping("/{id}/positions")
    public Result<Void> assignPositions(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> positionIds = (List<Integer>) body.get("positionIds");
        Integer primaryPositionId = (Integer) body.get("primaryPositionId");
        sysUserService.assignPositions(id, positionIds, primaryPositionId);
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Integer id) {
        sysUserService.resetPassword(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        sysUserService.updateStatus(id, status);
        return Result.success();
    }

    @GetMapping("/{id}/positions")
    public Result<List<Integer>> getUserPositions(@PathVariable Integer id) {
        return Result.success(sysUserService.getPositionIdsByUserId(id));
    }
}
