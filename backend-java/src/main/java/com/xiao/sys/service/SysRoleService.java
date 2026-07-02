package com.xiao.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.dto.RoleDTO;
import com.xiao.sys.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    List<SysRole> getRoleList();

    PageResult<RoleDTO> getRolePage(RoleDTO query);

    SysRole createRole(RoleDTO dto);

    SysRole updateRole(Integer id, RoleDTO dto);

    void deleteRole(Integer id);

    void assignMenus(Integer id, List<Integer> menuIds);

    List<Integer> getMenuIdsByRoleId(Integer id);
}
