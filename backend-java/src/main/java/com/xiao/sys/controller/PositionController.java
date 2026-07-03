package com.xiao.sys.controller;

import com.xiao.sys.common.Result;
import com.xiao.sys.dto.DataScopeDTO;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.dto.PositionDTO;
import com.xiao.sys.entity.SysPosition;
import com.xiao.sys.service.SysPositionService;
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
@RequestMapping("/api/sys/position")
public class PositionController {

    private final SysPositionService sysPositionService;

    public PositionController(SysPositionService sysPositionService) {
        this.sysPositionService = sysPositionService;
    }

    @GetMapping({"", "/list"})
    public Result<List<PositionDTO>> getPositionList(@RequestParam(required = false) Integer orgId) {
        return Result.success(sysPositionService.getPositionList(orgId));
    }

    @GetMapping("/page")
    public Result<PageResult<PositionDTO>> getPositionPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer orgId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PositionDTO query = new PositionDTO();
        query.setName(name);
        query.setOrgId(orgId);
        query.setPageNum(page);
        query.setPageSize(size);
        return Result.success(sysPositionService.getPositionPage(query));
    }

    @PostMapping
    public Result<SysPosition> createPosition(@RequestBody PositionDTO dto) {
        return Result.success(sysPositionService.createPosition(dto));
    }

    @PutMapping("/{id}")
    public Result<SysPosition> updatePosition(@PathVariable Integer id, @RequestBody PositionDTO dto) {
        return Result.success(sysPositionService.updatePosition(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePosition(@PathVariable Integer id) {
        sysPositionService.deletePosition(id);
        return Result.success();
    }

    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> roleIds = (List<Integer>) body.get("roleIds");
        sysPositionService.assignRoles(id, roleIds);
        return Result.success();
    }

    @GetMapping("/{id}/roles")
    public Result<List<Integer>> getRoleIds(@PathVariable Integer id) {
        return Result.success(sysPositionService.getRoleIdsByPositionId(id));
    }

    @PutMapping("/{id}/data-scope")
    public Result<Void> setDataScope(@PathVariable Integer id, @RequestBody DataScopeDTO dto) {
        sysPositionService.setDataScope(id, dto);
        return Result.success();
    }

    @GetMapping("/{id}/data-scope")
    public Result<DataScopeDTO> getDataScope(@PathVariable Integer id) {
        return Result.success(sysPositionService.getDataScope(id));
    }

    @GetMapping("/{id}/permissions")
    public Result<java.util.Map<String, Object>> getPermissions(@PathVariable Integer id) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("roleIds", sysPositionService.getRoleIdsByPositionId(id));
        result.put("dataScope", sysPositionService.getDataScope(id));
        return Result.success(result);
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> savePermissions(@PathVariable Integer id, @RequestBody java.util.Map<String, Object> body) {
        // 保存角色权限
        @SuppressWarnings("unchecked")
        java.util.List<Integer> roleIds = (java.util.List<Integer>) body.get("roleIds");
        if (roleIds != null) {
            sysPositionService.assignRoles(id, roleIds);
        }

        // 保存数据权限 (前端传 dataScope 数字，后端转成 scopeType 字符串)
        if (body.containsKey("dataScope")) {
            Integer dataScopeNum = body.get("dataScope") instanceof Number
                    ? ((Number) body.get("dataScope")).intValue()
                    : null;
            if (dataScopeNum != null) {
                DataScopeDTO dataScopeDto = new DataScopeDTO();
                dataScopeDto.setScopeType(DataScopeDTO.fromDataScopeNumber(dataScopeNum));
                @SuppressWarnings("unchecked")
                java.util.List<Integer> customOrgIds = (java.util.List<Integer>) body.get("customOrgIds");
                dataScopeDto.setCustomOrgIds(customOrgIds);
                sysPositionService.setDataScope(id, dataScopeDto);
            }
        }
        return Result.success();
    }
}
