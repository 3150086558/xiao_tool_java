package com.xiao.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiao.sys.entity.SysUserPosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserPositionMapper extends BaseMapper<SysUserPosition> {

    @Select("""
            SELECT position_id FROM sys_user_position WHERE user_id = #{userId}
            """)
    List<Integer> selectPositionIdsByUserId(@Param("userId") Integer userId);

    @Select("""
            SELECT user_id FROM sys_user_position WHERE position_id = #{positionId}
            """)
    List<Integer> selectUserIdsByPositionId(@Param("positionId") Integer positionId);
}
