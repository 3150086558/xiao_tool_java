package com.xiao.sys.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 月度趋势统计 DTO
 */
@Data
public class MonthTrendDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 月份，格式：YYYY-MM
     */
    private String month;

    /**
     * 收入金额
     */
    private BigDecimal income;

    /**
     * 支出金额
     */
    private BigDecimal expense;
}