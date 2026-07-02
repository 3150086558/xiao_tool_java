package com.xiao.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    @JsonIgnore
    private String password;

    private String realName;

    private String email;

    private String phone;

    private Integer orgId;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
