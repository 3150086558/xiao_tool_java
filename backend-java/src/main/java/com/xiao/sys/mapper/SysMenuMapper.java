package com.xiao.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiao.sys.entity.SysMenu;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("""
            <script>
            SELECT DISTINCT m.* FROM sys_menu m
            INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
            WHERE rm.role_id IN
            <foreach collection="roleIds" item="rid" open="(" separator="," close=")">
                #{rid}
            </foreach>
            AND m.status = 1
            ORDER BY m.sort_order ASC, m.id ASC
            </script>
            """)
    List<SysMenu> selectMenusByRoleIds(@Param("roleIds") List<Integer> roleIds);

    @Select("""
            <script>
            SELECT DISTINCT m.* FROM sys_menu m
            INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
            INNER JOIN sys_position_role pr ON rm.role_id = pr.role_id
            WHERE pr.position_id IN
            <foreach collection="positionIds" item="pid" open="(" separator="," close=")">
                #{pid}
            </foreach>
            AND m.status = 1
            ORDER BY m.sort_order ASC, m.id ASC
            </script>
            """)
    List<SysMenu> selectMenusByPositionIds(@Param("positionIds") List<Integer> positionIds);

    @Insert("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (#{roleId}, #{menuId}) ON CONFLICT DO NOTHING")
    void insertRoleMenu(@Param("roleId") Integer roleId, @Param("menuId") Integer menuId);
}
