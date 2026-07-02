package com.xiao.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_org")
public class SysOrg {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer parentId;

    private String orgName;

    private String orgCode;

    private Integer sortOrder;

    private Integer status;

    private Integer leaderId;

    private String leader;

    private String phone;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
