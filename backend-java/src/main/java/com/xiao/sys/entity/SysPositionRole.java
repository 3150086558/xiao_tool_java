package com.xiao.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_position_role")
public class SysPositionRole {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer positionId;

    private Integer roleId;

    private LocalDateTime createdAt;
}
