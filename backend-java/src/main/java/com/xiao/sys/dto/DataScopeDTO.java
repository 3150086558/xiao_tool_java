package com.xiao.sys.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class DataScopeDTO {

    private Integer positionId;

    @NotBlank(message = "数据范围类型不能为空")
    private String scopeType;

    private List<Integer> customOrgIds;
}
