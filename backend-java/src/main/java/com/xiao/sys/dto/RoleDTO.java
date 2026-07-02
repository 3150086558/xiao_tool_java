package com.xiao.sys.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoleDTO {

    private Integer id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;

    private List<Integer> menuIds;
}
