package com.xiao.sys.controller;

import com.xiao.sys.common.Result;
import com.xiao.sys.dto.DataScopeDTO;
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

    @GetMapping
    public Result<List<PositionDTO>> getPositionList(@RequestParam(required = false) Integer orgId) {
        return Result.success(sysPositionService.getPositionList(orgId));
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
}
