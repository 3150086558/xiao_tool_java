package com.xiao.sys.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 统计汇总结果 DTO
 */
@Data
public class StatsSummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收入总额
     */
    private BigDecimal income;

    /**
     * 支出总额
     */
    private BigDecimal expense;

    /**
     * 结余
     */
    private BigDecimal balance;

    /**
     * 分类统计列表
     */
    private List<CategoryStatsDTO> categories;

    /**
     * 分类统计项
     */
    @Data
    public static class CategoryStatsDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 类型：income/expense
         */
        private String type;

        /**
         * 分类名称
         */
        private String category;

        /**
         * 金额
         */
        private BigDecimal amount;

        /**
         * 记录数量
         */
        private Integer count;
    }
}