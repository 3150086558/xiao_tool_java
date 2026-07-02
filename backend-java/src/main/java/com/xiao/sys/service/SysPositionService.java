package com.xiao.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiao.sys.dto.DataScopeDTO;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.dto.PositionDTO;
import com.xiao.sys.entity.SysPosition;

import java.util.List;

public interface SysPositionService extends IService<SysPosition> {

    List<PositionDTO> getPositionList(Integer orgId);

    PageResult<PositionDTO> getPositionPage(PositionDTO query);

    SysPosition createPosition(PositionDTO dto);

    SysPosition updatePosition(Integer id, PositionDTO dto);

    void deletePosition(Integer id);

    void assignRoles(Integer id, List<Integer> roleIds);

    List<Integer> getRoleIdsByPositionId(Integer id);

    void setDataScope(Integer id, DataScopeDTO dto);

    DataScopeDTO getDataScope(Integer id);
}
