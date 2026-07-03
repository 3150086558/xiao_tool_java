package com.xiao.sys.service.impl;

import com.xiao.sys.dto.MonthTrendDTO;
import com.xiao.sys.dto.StatsSummaryDTO;
import com.xiao.sys.mapper.AppRecordMapper;
import com.xiao.sys.service.StatsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务实现类
 */
@Service
public class StatsServiceImpl implements StatsService {

    private final AppRecordMapper recordMapper;

    public StatsServiceImpl(AppRecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    @Override
    public StatsSummaryDTO getSummary(List<Integer> userIds, String month) {
        // 查询收入支出汇总
        List<Map<String, Object>> summaryRows = recordMapper.summaryByType(userIds, month);

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (Map<String, Object> row : summaryRows) {
            String type = (String) row.get("type");
            Object totalObj = row.get("total");
            BigDecimal total = toBigDecimal(totalObj);
            
            if ("income".equals(type)) {
                income = total;
            } else if ("expense".equals(type)) {
                expense = total;
            }
        }

        // 查询分类统计
        List<Map<String, Object>> categoryRows = recordMapper.summaryByCategory(userIds, month, null);
        List<StatsSummaryDTO.CategoryStatsDTO> categories = new ArrayList<>();

        for (Map<String, Object> row : categoryRows) {
            StatsSummaryDTO.CategoryStatsDTO dto = new StatsSummaryDTO.CategoryStatsDTO();
            dto.setType((String) row.get("type"));
            dto.setCategory((String) row.get("category"));
            dto.setAmount(toBigDecimal(row.get("amount")));
            dto.setCount(((Number) row.get("count")).intValue());
            categories.add(dto);
        }

        StatsSummaryDTO result = new StatsSummaryDTO();
        result.setIncome(income);
        result.setExpense(expense);
        result.setBalance(income.subtract(expense));
        result.setCategories(categories);

        return result;
    }

    @Override
    public List<StatsSummaryDTO.CategoryStatsDTO> getCategoryStats(List<Integer> userIds, String month, String type) {
        List<Map<String, Object>> categoryRows = recordMapper.summaryByCategory(userIds, month, type);
        List<StatsSummaryDTO.CategoryStatsDTO> categories = new ArrayList<>();

        for (Map<String, Object> row : categoryRows) {
            // 如果指定了类型，只返回该类型的数据
            if (type != null && !type.isEmpty() && !type.equals(row.get("type"))) {
                continue;
            }
            StatsSummaryDTO.CategoryStatsDTO dto = new StatsSummaryDTO.CategoryStatsDTO();
            dto.setType((String) row.get("type"));
            dto.setCategory((String) row.get("category"));
            dto.setAmount(toBigDecimal(row.get("amount")));
            dto.setCount(((Number) row.get("count")).intValue());
            categories.add(dto);
        }

        return categories;
    }

    @Override
    public List<MonthTrendDTO> getTrend(List<Integer> userIds, String month) {
        List<Map<String, Object>> trendRows = recordMapper.trendByMonth(userIds, month);

        // 按月份汇总收入支出
        Map<String, MonthTrendDTO> resultMap = new HashMap<>();

        for (Map<String, Object> row : trendRows) {
            String monthKey = (String) row.get("month");
            String type = (String) row.get("type");
            BigDecimal amount = toBigDecimal(row.get("amount"));

            MonthTrendDTO dto = resultMap.get(monthKey);
            if (dto == null) {
                dto = new MonthTrendDTO();
                dto.setMonth(monthKey);
                dto.setIncome(BigDecimal.ZERO);
                dto.setExpense(BigDecimal.ZERO);
                resultMap.put(monthKey, dto);
            }

            if ("income".equals(type)) {
                dto.setIncome(amount);
            } else if ("expense".equals(type)) {
                dto.setExpense(amount);
            }
        }

        // 按月份排序
        List<MonthTrendDTO> result = new ArrayList<>(resultMap.values());
        result.sort((a, b) -> a.getMonth().compareTo(b.getMonth()));

        return result;
    }

    /**
     * 将对象转换为 BigDecimal
     */
    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) {
            return BigDecimal.ZERO;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        if (obj instanceof Number) {
            return BigDecimal.valueOf(((Number) obj).doubleValue());
        }
        try {
            return new BigDecimal(obj.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}