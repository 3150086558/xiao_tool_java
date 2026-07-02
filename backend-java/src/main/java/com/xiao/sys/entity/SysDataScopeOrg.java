package com.xiao.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_data_scope_org")
public class SysDataScopeOrg {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer scopeId;

    private Integer orgId;
}
