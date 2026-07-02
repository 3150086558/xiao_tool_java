package com.xiao.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer parentId;

    private String menuName;

    private String menuType;

    private String path;

    private String component;

    private String permission;

    private String icon;

    private Integer sortOrder;

    private Integer visible;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
