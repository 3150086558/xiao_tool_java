package com.xiao.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_position_data_scope")
public class SysPositionDataScope {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer positionId;

    private String scopeType;

    private LocalDateTime createdAt;
}
