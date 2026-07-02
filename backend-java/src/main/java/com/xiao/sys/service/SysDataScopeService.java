package com.xiao.sys.service;

import com.xiao.sys.dto.DataScopeDTO;

import java.util.List;

public interface SysDataScopeService {

    List<Integer> getVisibleUserIds(Integer userId);

    List<Integer> getVisibleOrgIds(Integer userId);

    void setDataScope(Integer positionId, DataScopeDTO dto);

    DataScopeDTO getDataScope(Integer positionId);
}
