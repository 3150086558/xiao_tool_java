package com.xiao.sys.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserInfoDTO {

    private Integer id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private Integer orgId;
    private String orgName;
    private Integer status;

    private List<String> permissions;
    private List<MenuTreeNode> menus;
    private List<String> roles;
    private List<Integer> positionIds;
    private List<Map<String, Object>> dataScopes;
    private List<Integer> visibleOrgIds;
}
