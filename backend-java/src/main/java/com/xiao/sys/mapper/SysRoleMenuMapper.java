package com.xiao.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiao.sys.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    @Select("""
            SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}
            """)
    List<Integer> selectMenuIdsByRoleId(@Param("roleId") Integer roleId);

    @Select("""
            <script>
            SELECT DISTINCT menu_id FROM sys_role_menu WHERE role_id IN
            <foreach collection="roleIds" item="rid" open="(" separator="," close=")">
                #{rid}
            </foreach>
            </script>
            """)
    List<Integer> selectMenuIdsByRoleIds(@Param("roleIds") List<Integer> roleIds);
}
