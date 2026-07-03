package com.xiao.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiao.sys.entity.AppNote;
import org.apache.ibatis.annotations.Mapper;

/**
 * 备忘录 Mapper 接口
 */
@Mapper
public interface AppNoteMapper extends BaseMapper<AppNote> {
}