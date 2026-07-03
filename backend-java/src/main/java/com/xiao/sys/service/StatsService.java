package com.xiao.sys.service;

import com.xiao.sys.dto.MonthTrendDTO;
import com.xiao.sys.dto.StatsSummaryDTO;

import java.util.List;

/**
 * 统计服务接口
 */
public interface StatsService {

    /**
     * 获取统计汇总
     * @param userIds 可见的用户ID列表
     * @param month 月份过滤（可选）
     * @return 统计汇总结果
     */
    StatsSummaryDTO getSummary(List<Integer> userIds, String month);

    /**
     * 获取分类统计
     * @param userIds 可见的用户ID列表
     * @param month 月份过滤（可选）
     * @param type 类型过滤（可选）
     * @return 分类统计列表
     */
    List<StatsSummaryDTO.CategoryStatsDTO> getCategoryStats(List<Integer> userIds, String month, String type);

    /**
     * 获取月度趋势
     * @param userIds 可见的用户ID列表
     * @param month 月份过滤（可选）
     * @return 月度趋势列表
     */
    List<MonthTrendDTO> getTrend(List<Integer> userIds, String month);
}