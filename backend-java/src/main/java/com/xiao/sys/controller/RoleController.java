package com.xiao.sys.controller;

import com.xiao.sys.common.Result;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.dto.RoleDTO;
import com.xiao.sys.entity.SysRole;
import com.xiao.sys.service.SysRoleService;
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
@RequestMapping("/api/sys/role")
public class RoleController {

    private final SysRoleService sysRoleService;

    public RoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @GetMapping
    public Result<List<SysRole>> getRoleList() {
        return Result.success(sysRoleService.getRoleList());
    }

    @GetMapping("/page")
    public Result<PageResult<RoleDTO>> getRolePage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        RoleDTO query = new RoleDTO();
        query.setName(name);
        query.setCode(code);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        return Result.success(sysRoleService.getRolePage(query));
    }

    @PostMapping
    public Result<SysRole> createRole(@RequestBody RoleDTO dto) {
        return Result.success(sysRoleService.createRole(dto));
    }

    @PutMapping("/{id}")
    public Result<SysRole> updateRole(@PathVariable Integer id, @RequestBody RoleDTO dto) {
        return Result.success(sysRoleService.updateRole(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Integer id) {
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> menuIds = (List<Integer>) body.get("menuIds");
        sysRoleService.assignMenus(id, menuIds);
        return Result.success();
    }

    @GetMapping("/{id}/menus")
    public Result<List<Integer>> getMenuIds(@PathVariable Integer id) {
        return Result.success(sysRoleService.getMenuIdsByRoleId(id));
    }
}
