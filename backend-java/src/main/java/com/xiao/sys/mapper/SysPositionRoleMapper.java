package com.xiao.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiao.sys.entity.SysPositionRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPositionRoleMapper extends BaseMapper<SysPositionRole> {

    @Select("""
            SELECT role_id FROM sys_position_role WHERE position_id = #{positionId}
            """)
    List<Integer> selectRoleIdsByPositionId(@Param("positionId") Integer positionId);

    @Select("""
            <script>
            SELECT DISTINCT pr.role_id FROM sys_position_role pr
            WHERE pr.position_id IN
            <foreach collection="positionIds" item="pid" open="(" separator="," close=")">
                #{pid}
            </foreach>
            </script>
            """)
    List<Integer> selectRoleIdsByPositionIds(@Param("positionIds") List<Integer> positionIds);
}
