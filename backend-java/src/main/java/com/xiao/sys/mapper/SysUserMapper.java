package com.xiao.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiao.sys.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("""
            <script>
            SELECT DISTINCT u.* FROM sys_user u
            LEFT JOIN sys_org o ON u.org_id = o.id
            WHERE 1=1
            <if test="keyword != null and keyword != ''">
                AND (u.username LIKE CONCAT('%', #{keyword}, '%')
                  OR u.real_name LIKE CONCAT('%', #{keyword}, '%')
                  OR u.phone LIKE CONCAT('%', #{keyword}, '%')
                  OR u.email LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="orgId != null">
                AND u.org_id = #{orgId}
            </if>
            <if test="status != null">
                AND u.status = #{status}
            </if>
            ORDER BY u.id DESC
            </script>
            """)
    List<SysUser> selectUserList(@Param("keyword") String keyword,
                                 @Param("orgId") Integer orgId,
                                 @Param("status") Integer status);

    @Select("""
            SELECT u.* FROM sys_user u
            WHERE u.org_id = #{orgId}
            ORDER BY u.id ASC
            """)
    List<SysUser> selectUsersByOrgId(@Param("orgId") Integer orgId);

    @Select("""
            <script>
            SELECT DISTINCT u.id FROM sys_user u
            WHERE u.status = 1 AND (
            <if test="orgIds != null and orgIds.size() > 0">
                u.org_id IN
                <foreach collection="orgIds" item="oid" open="(" separator="," close=")">
                    #{oid}
                </foreach>
            </if>
            <if test="orgIds == null or orgIds.size() == 0">
                1 = 0
            </if>
            <if test="selfUserId != null">
                OR u.id = #{selfUserId}
            </if>
            )
            </script>
            """)
    List<Integer> selectUserIdsByOrgIds(@Param("orgIds") List<Integer> orgIds,
                                        @Param("selfUserId") Integer selfUserId);

    @Select("""
            SELECT u.* FROM sys_user u WHERE u.username = #{username}
            """)
    SysUser selectByUsername(@Param("username") String username);
}
