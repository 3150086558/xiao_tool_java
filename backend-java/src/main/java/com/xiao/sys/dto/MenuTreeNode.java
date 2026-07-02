package com.xiao.sys.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuTreeNode {

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
    private List<MenuTreeNode> children = new ArrayList<>();

    public void addChild(MenuTreeNode child) {
        this.children.add(child);
    }
}
