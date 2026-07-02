package com.xiao.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_oper_log")
public class SysOperLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String username;

    private String operModule;

    private String operType;

    private String operDesc;

    private String operIp;

    private LocalDateTime operTime;

    private Integer costMs;
}
