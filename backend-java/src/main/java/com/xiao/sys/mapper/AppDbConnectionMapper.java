package com.xiao.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiao.sys.entity.AppDbConnection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据库连接Mapper
 */
@Mapper
public interface AppDbConnectionMapper extends BaseMapper<AppDbConnection> {
}
