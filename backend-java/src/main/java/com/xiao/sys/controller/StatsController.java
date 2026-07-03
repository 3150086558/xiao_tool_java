package com.xiao.sys.controller;

import com.xiao.sys.common.Result;
import com.xiao.sys.dto.MonthTrendDTO;
import com.xiao.sys.dto.StatsSummaryDTO;
import com.xiao.sys.service.StatsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xiao.sys.security.LoginUser;

import java.util.Collections;
import java.util.List;

/**
 * 统计控制器
 * 路径：/api/app/stats/*
 */
@RestController
@RequestMapping("/api/app/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * 获取统计汇总
     * GET /api/app/stats/summary
     * @param month 月份过滤，如 2025-06
     * @param loginUser 当前登录用户
     * @return 统计汇总结果
     */
    @GetMapping("/summary")
    public Result<StatsSummaryDTO> getSummary(
            @RequestParam(required = false, defaultValue = "") String month,
            @AuthenticationPrincipal LoginUser loginUser) {
        // 获取当前用户可见的用户ID列表（目前只查询自己的数据）
        List<Integer> userIds = Collections.singletonList(loginUser.getUserId());
        
        String filteredMonth = month != null ? month.trim() : "";
        StatsSummaryDTO result = statsService.getSummary(userIds, filteredMonth.isEmpty() ? null : filteredMonth);
        
        return Result.success(result);
    }

    /**
     * 获取分类统计
     * GET /api/app/stats/category
     * @param type 类型过滤：income/expense
     * @param month 月份过滤，如 2025-06
     * @param loginUser 当前登录用户
     * @return 分类统计列表
     */
    @GetMapping("/category")
    public Result<List<StatsSummaryDTO.CategoryStatsDTO>> getCategoryStats(
            @RequestParam(required = false, defaultValue = "") String type,
            @RequestParam(required = false, defaultValue = "") String month,
            @AuthenticationPrincipal LoginUser loginUser) {
        List<Integer> userIds = Collections.singletonList(loginUser.getUserId());
        
        String filteredMonth = month != null ? month.trim() : "";
        String filteredType = type != null ? type.trim() : "";
        
        List<StatsSummaryDTO.CategoryStatsDTO> result = statsService.getCategoryStats(
                userIds,
                filteredMonth.isEmpty() ? null : filteredMonth,
                filteredType.isEmpty() ? null : filteredType
        );
        
        return Result.success(result);
    }

    /**
     * 获取月度趋势
     * GET /api/app/stats/trend
     * @param month 月份过滤，如 2025-06
     * @param loginUser 当前登录用户
     * @return 月度趋势列表
     */
    @GetMapping("/trend")
    public Result<List<MonthTrendDTO>> getTrend(
            @RequestParam(required = false, defaultValue = "") String month,
            @AuthenticationPrincipal LoginUser loginUser) {
        List<Integer> userIds = Collections.singletonList(loginUser.getUserId());
        
        String filteredMonth = month != null ? month.trim() : "";
        List<MonthTrendDTO> result = statsService.getTrend(
                userIds,
                filteredMonth.isEmpty() ? null : filteredMonth
        );
        
        return Result.success(result);
    }
}