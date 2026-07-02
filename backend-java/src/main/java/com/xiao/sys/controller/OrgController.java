package com.xiao.sys.controller;

import com.xiao.sys.common.Result;
import com.xiao.sys.dto.OrgDTO;
import com.xiao.sys.entity.SysOrg;
import com.xiao.sys.entity.SysUser;
import com.xiao.sys.service.SysOrgService;
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
@RequestMapping("/api/sys/org")
public class OrgController {

    private final SysOrgService sysOrgService;

    public OrgController(SysOrgService sysOrgService) {
        this.sysOrgService = sysOrgService;
    }

    @GetMapping("/tree")
    public Result<List<OrgDTO>> getOrgTree() {
        return Result.success(sysOrgService.getOrgTree());
    }

    @PostMapping
    public Result<SysOrg> createOrg(@RequestBody OrgDTO dto) {
        return Result.success(sysOrgService.createOrg(dto));
    }

    @PutMapping("/{id}")
    public Result<SysOrg> updateOrg(@PathVariable Integer id, @RequestBody OrgDTO dto) {
        return Result.success(sysOrgService.updateOrg(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteOrg(@PathVariable Integer id) {
        sysOrgService.deleteOrg(id);
        return Result.success();
    }

    @GetMapping("/{id}/users")
    public Result<List<SysUser>> getUsersByOrgId(@PathVariable Integer id) {
        return Result.success(sysOrgService.getUsersByOrgId(id));
    }
}
