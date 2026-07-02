package com.xiao.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiao.sys.entity.SysPositionDataScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPositionDataScopeMapper extends BaseMapper<SysPositionDataScope> {

    @Select("""
            <script>
            SELECT * FROM sys_position_data_scope WHERE position_id IN
            <foreach collection="positionIds" item="pid" open="(" separator="," close=")">
                #{pid}
            </foreach>
            </script>
            """)
    List<SysPositionDataScope> selectByPositionIds(@Param("positionIds") List<Integer> positionIds);
}
