package com.xiao.sys.controller;

import com.xiao.sys.common.BusinessException;
import com.xiao.sys.common.Result;
import com.xiao.sys.common.ResultCode;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.dto.RecordDTO;
import com.xiao.sys.entity.AppRecord;
import com.xiao.sys.security.LoginUser;
import com.xiao.sys.service.AppRecordService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 记账管理控制器
 */
@RestController
@RequestMapping("/api/app")
public class RecordController {

    private final AppRecordService appRecordService;

    public RecordController(AppRecordService appRecordService) {
        this.appRecordService = appRecordService;
    }

    /**
     * 获取当前登录用户
     */
    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return (LoginUser) authentication.getPrincipal();
    }

    /**
     * 分页查询记账记录
     */
    @GetMapping("/accounting/page")
    public Result<Map<String, Object>> getRecordPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String item,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LoginUser loginUser = getLoginUser();
        Integer userId = loginUser.getUserId();

        PageResult<RecordDTO> pageResult = appRecordService.getRecordPage(
                userId, item, category, startDate, endDate, page, size
        );

        Map<String, Object> data = new HashMap<>();
        data.put("records", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("size", size);

        return Result.success(data);
    }

    /**
     * 创建记账记录
     */
    @PostMapping("/accounting")
    public Result<RecordDTO> createRecord(@RequestBody RecordDTO dto) {
        LoginUser loginUser = getLoginUser();
        Integer userId = loginUser.getUserId();

        AppRecord record = appRecordService.createRecord(userId, dto);

        RecordDTO result = new RecordDTO();
        result.setId(record.getId());
        result.setRecordDate(record.getRecordDate());
        result.setType(record.getType());
        result.setCategory(record.getCategory());
        result.setSubCategory(record.getSubCategory());
        result.setAmount(record.getAmount());
        result.setAccount(record.getAccount());
        result.setNote(record.getNote());
        result.setUserId(record.getUserId());
        result.setOrgId(record.getOrgId());
        result.setCreatedAt(record.getCreatedAt());
        result.setUpdatedAt(record.getUpdatedAt());

        return Result.success(result);
    }

    /**
     * 更新记账记录
     */
    @PutMapping("/accounting/{recordId}")
    public Result<RecordDTO> updateRecord(@PathVariable Integer recordId, @RequestBody RecordDTO dto) {
        LoginUser loginUser = getLoginUser();
        Integer userId = loginUser.getUserId();

        AppRecord record = appRecordService.updateRecord(userId, recordId, dto);

        RecordDTO result = new RecordDTO();
        result.setId(record.getId());
        result.setRecordDate(record.getRecordDate());
        result.setType(record.getType());
        result.setCategory(record.getCategory());
        result.setSubCategory(record.getSubCategory());
        result.setAmount(record.getAmount());
        result.setAccount(record.getAccount());
        result.setNote(record.getNote());
        result.setUserId(record.getUserId());
        result.setOrgId(record.getOrgId());
        result.setCreatedAt(record.getCreatedAt());
        result.setUpdatedAt(record.getUpdatedAt());

        return Result.success(result);
    }

    /**
     * 删除记账记录
     */
    @DeleteMapping("/accounting/{recordId}")
    public Result<Map<String, Object>> deleteRecord(@PathVariable Integer recordId) {
        LoginUser loginUser = getLoginUser();
        Integer userId = loginUser.getUserId();

        boolean deleted = appRecordService.deleteRecord(userId, recordId);
        if (!deleted) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "记录不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("ok", true);

        return Result.success(data);
    }

    /**
     * 删除所有记账记录
     */
    @DeleteMapping("/accounting/all")
    public Result<Map<String, Object>> deleteAllRecords() {
        LoginUser loginUser = getLoginUser();
        Integer userId = loginUser.getUserId();

        int count = appRecordService.deleteAllRecords(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("deleted", count);

        return Result.success(data);
    }
}