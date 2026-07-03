package com.xiao.sys.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 导入结果 DTO
 */
@Data
public class ImportResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功导入的数量
     */
    private Integer success;

    /**
     * 错误信息列表
     */
    private List<String> errors;

    public ImportResultDTO() {
    }

    public ImportResultDTO(Integer success, List<String> errors) {
        this.success = success;
        this.errors = errors;
    }
}