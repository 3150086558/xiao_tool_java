package com.xiao.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user_position")
public class SysUserPosition {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer positionId;

    private Boolean isPrimary;

    private LocalDateTime createdAt;
}
