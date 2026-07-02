package com.xiao.sys.controller;

import com.xiao.sys.common.Result;
import com.xiao.sys.dto.MenuDTO;
import com.xiao.sys.dto.MenuTreeNode;
import com.xiao.sys.entity.SysMenu;
import com.xiao.sys.security.LoginUser;
import com.xiao.sys.service.SysMenuService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sys/menu")
public class MenuController {

    private final SysMenuService sysMenuService;

    public MenuController(SysMenuService sysMenuService) {
        this.sysMenuService = sysMenuService;
    }

    @GetMapping("/tree")
    public Result<List<MenuTreeNode>> getMenuTree() {
        return Result.success(sysMenuService.getMenuTree());
    }

    @GetMapping("/user-tree")
    public Result<List<MenuTreeNode>> getUserMenuTree() {
        Integer userId = getCurrentUserId();
        return Result.success(sysMenuService.getUserMenuTree(userId));
    }

    @PostMapping
    public Result<SysMenu> createMenu(@RequestBody MenuDTO dto) {
        return Result.success(sysMenuService.createMenu(dto));
    }

    @PutMapping("/{id}")
    public Result<SysMenu> updateMenu(@PathVariable Integer id, @RequestBody MenuDTO dto) {
        return Result.success(sysMenuService.updateMenu(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMenu(@PathVariable Integer id) {
        sysMenuService.deleteMenu(id);
        return Result.success();
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}
