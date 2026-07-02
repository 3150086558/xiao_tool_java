package com.xiao.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_position")
public class SysPosition {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer orgId;

    private String positionName;

    private String positionCode;

    private Integer sortOrder;

    private Integer status;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
