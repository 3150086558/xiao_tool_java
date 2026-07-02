package com.xiao.sys.dto;

import lombok.Data;

import java.util.List;

@Data
public class PositionDTO {

    private Integer id;
    private Integer orgId;
    private String orgName;
    private String positionName;
    private String positionCode;
    private Integer sortOrder;
    private Integer status;
    private String description;

    private List<Integer> roleIds;
    private String scopeType;
    private List<Integer> customOrgIds;
}
