package com.xiao.sys.dto;

import lombok.Data;

@Data
public class MenuDTO {

    private Integer id;
    private Integer parentId;
    private String menuName;
    private String title;
    private String menuType;
    private Integer type;
    private String path;
    private String component;
    private String permission;
    private String icon;
    private Integer sortOrder;
    private Integer visible;
    private Integer status;
}
