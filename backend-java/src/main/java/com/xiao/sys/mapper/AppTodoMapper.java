package com.xiao.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiao.sys.entity.AppTodo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 待办事项 Mapper 接口
 */
@Mapper
public interface AppTodoMapper extends BaseMapper<AppTodo> {
}