package com.xiao.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiao.sys.entity.SysDataScopeOrg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysDataScopeOrgMapper extends BaseMapper<SysDataScopeOrg> {

    @Select("""
            SELECT org_id FROM sys_data_scope_org WHERE scope_id = #{scopeId}
            """)
    List<Integer> selectOrgIdsByScopeId(@Param("scopeId") Integer scopeId);

    @Select("""
            <script>
            SELECT DISTINCT org_id FROM sys_data_scope_org WHERE scope_id IN
            <foreach collection="scopeIds" item="sid" open="(" separator="," close=")">
                #{sid}
            </foreach>
            </script>
            """)
    List<Integer> selectOrgIdsByScopeIds(@Param("scopeIds") List<Integer> scopeIds);
}
