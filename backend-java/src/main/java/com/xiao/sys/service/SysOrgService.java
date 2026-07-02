package com.xiao.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiao.sys.dto.OrgDTO;
import com.xiao.sys.entity.SysOrg;
import com.xiao.sys.entity.SysUser;

import java.util.List;

public interface SysOrgService extends IService<SysOrg> {

    List<OrgDTO> getOrgTree();

    SysOrg createOrg(OrgDTO dto);

    SysOrg updateOrg(Integer id, OrgDTO dto);

    void deleteOrg(Integer id);

    List<SysUser> getUsersByOrgId(Integer orgId);

    List<OrgDTO> buildTree(List<SysOrg> orgs);

    List<Integer> getChildOrgIds(Integer parentId);
}
