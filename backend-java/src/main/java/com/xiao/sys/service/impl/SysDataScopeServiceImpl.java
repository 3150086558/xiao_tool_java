package com.xiao.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.sys.common.BusinessException;
import com.xiao.sys.common.ResultCode;
import com.xiao.sys.dto.DataScopeDTO;
import com.xiao.sys.entity.SysDataScopeOrg;
import com.xiao.sys.entity.SysOrg;
import com.xiao.sys.entity.SysPositionDataScope;
import com.xiao.sys.mapper.SysDataScopeOrgMapper;
import com.xiao.sys.mapper.SysOrgMapper;
import com.xiao.sys.mapper.SysPositionDataScopeMapper;
import com.xiao.sys.mapper.SysUserMapper;
import com.xiao.sys.mapper.SysUserPositionMapper;
import com.xiao.sys.service.SysDataScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysDataScopeServiceImpl implements SysDataScopeService {

    private final SysUserPositionMapper sysUserPositionMapper;
    private final SysPositionDataScopeMapper sysPositionDataScopeMapper;
    private final SysDataScopeOrgMapper sysDataScopeOrgMapper;
    private final SysOrgMapper sysOrgMapper;
    private final SysUserMapper sysUserMapper;

    public SysDataScopeServiceImpl(SysUserPositionMapper sysUserPositionMapper,
                                   SysPositionDataScopeMapper sysPositionDataScopeMapper,
                                   SysDataScopeOrgMapper sysDataScopeOrgMapper,
                                   SysOrgMapper sysOrgMapper,
                                   SysUserMapper sysUserMapper) {
        this.sysUserPositionMapper = sysUserPositionMapper;
        this.sysPositionDataScopeMapper = sysPositionDataScopeMapper;
        this.sysDataScopeOrgMapper = sysDataScopeOrgMapper;
        this.sysOrgMapper = sysOrgMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public List<Integer> getVisibleOrgIds(Integer userId) {
        List<Integer> positionIds = sysUserPositionMapper.selectPositionIdsByUserId(userId);
        if (positionIds == null || positionIds.isEmpty()) {
            return List.of();
        }

        List<SysPositionDataScope> scopes = sysPositionDataScopeMapper.selectByPositionIds(positionIds);
        if (scopes == null || scopes.isEmpty()) {
            return List.of();
        }

        Set<Integer> resultOrgIds = new HashSet<>();
        Integer userOrgId = getUserOrgId(userId);

        for (SysPositionDataScope scope : scopes) {
            String scopeType = scope.getScopeType();
            switch (scopeType) {
                case "all" -> {
                    List<SysOrg> allOrgs = sysOrgMapper.selectList(null);
                    resultOrgIds.addAll(allOrgs.stream().map(SysOrg::getId).collect(Collectors.toSet()));
                }
                case "self" -> {
                    if (userOrgId != null) {
                        resultOrgIds.add(userOrgId);
                    }
                }
                case "dept" -> {
                    if (userOrgId != null) {
                        resultOrgIds.add(userOrgId);
                    }
                }
                case "dept_and_sub" -> {
                    if (userOrgId != null) {
                        resultOrgIds.add(userOrgId);
                        resultOrgIds.addAll(getChildOrgIds(userOrgId));
                    }
                }
                case "custom" -> {
                    List<Integer> customOrgIds = sysDataScopeOrgMapper.selectOrgIdsByScopeId(scope.getId());
                    resultOrgIds.addAll(customOrgIds);
                }
                default -> {
                    if (userOrgId != null) {
                        resultOrgIds.add(userOrgId);
                    }
                }
            }
        }
        return new ArrayList<>(resultOrgIds);
    }

    @Override
    public List<Integer> getVisibleUserIds(Integer userId) {
        List<Integer> visibleOrgIds = getVisibleOrgIds(userId);
        if (visibleOrgIds.isEmpty()) {
            return List.of(userId);
        }
        List<Integer> userIds = sysUserMapper.selectUserIdsByOrgIds(visibleOrgIds, userId);
        Set<Integer> result = new HashSet<>(userIds);
        result.add(userId);
        return new ArrayList<>(result);
    }

    @Override
    @Transactional
    public void setDataScope(Integer positionId, DataScopeDTO dto) {
        LambdaQueryWrapper<SysPositionDataScope> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPositionDataScope::getPositionId, positionId);
        SysPositionDataScope existing = sysPositionDataScopeMapper.selectOne(wrapper);

        SysPositionDataScope scope;
        if (existing != null) {
            scope = existing;
            scope.setScopeType(dto.getScopeType());
        } else {
            scope = new SysPositionDataScope();
            scope.setPositionId(positionId);
            scope.setScopeType(dto.getScopeType());
            scope.setCreatedAt(LocalDateTime.now());
            sysPositionDataScopeMapper.insert(scope);
        }

        LambdaQueryWrapper<SysDataScopeOrg> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SysDataScopeOrg::getScopeId, scope.getId());
        sysDataScopeOrgMapper.delete(deleteWrapper);

        if ("custom".equals(dto.getScopeType()) && dto.getCustomOrgIds() != null && !dto.getCustomOrgIds().isEmpty()) {
            for (Integer orgId : dto.getCustomOrgIds()) {
                SysDataScopeOrg dso = new SysDataScopeOrg();
                dso.setScopeId(scope.getId());
                dso.setOrgId(orgId);
                sysDataScopeOrgMapper.insert(dso);
            }
        }

        if (existing != null) {
            sysPositionDataScopeMapper.updateById(scope);
        }
    }

    @Override
    public DataScopeDTO getDataScope(Integer positionId) {
        LambdaQueryWrapper<SysPositionDataScope> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPositionDataScope::getPositionId, positionId);
        SysPositionDataScope scope = sysPositionDataScopeMapper.selectOne(wrapper);

        DataScopeDTO dto = new DataScopeDTO();
        dto.setPositionId(positionId);
        if (scope != null) {
            dto.setScopeType(scope.getScopeType());
            if ("custom".equals(scope.getScopeType())) {
                List<Integer> orgIds = sysDataScopeOrgMapper.selectOrgIdsByScopeId(scope.getId());
                dto.setCustomOrgIds(orgIds);
            }
        } else {
            dto.setScopeType("self");
        }
        return dto;
    }

    private Integer getUserOrgId(Integer userId) {
        com.xiao.sys.entity.SysUser user = sysUserMapper.selectById(userId);
        return user != null ? user.getOrgId() : null;
    }

    private List<Integer> getChildOrgIds(Integer parentId) {
        List<Integer> result = new ArrayList<>();
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrg::getParentId, parentId);
        List<SysOrg> children = sysOrgMapper.selectList(wrapper);
        for (SysOrg child : children) {
            result.add(child.getId());
            result.addAll(getChildOrgIds(child.getId()));
        }
        return result;
    }
}
