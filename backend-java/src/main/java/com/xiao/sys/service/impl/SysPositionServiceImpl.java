package com.xiao.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiao.sys.common.BusinessException;
import com.xiao.sys.common.ResultCode;
import com.xiao.sys.dto.DataScopeDTO;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.dto.PositionDTO;
import com.xiao.sys.entity.SysOrg;
import com.xiao.sys.entity.SysPosition;
import com.xiao.sys.entity.SysPositionRole;
import com.xiao.sys.entity.SysUserPosition;
import com.xiao.sys.mapper.SysOrgMapper;
import com.xiao.sys.mapper.SysPositionMapper;
import com.xiao.sys.mapper.SysPositionRoleMapper;
import com.xiao.sys.mapper.SysUserPositionMapper;
import com.xiao.sys.service.SysDataScopeService;
import com.xiao.sys.service.SysPositionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysPositionServiceImpl extends ServiceImpl<SysPositionMapper, SysPosition> implements SysPositionService {

    private final SysPositionRoleMapper sysPositionRoleMapper;
    private final SysUserPositionMapper sysUserPositionMapper;
    private final SysOrgMapper sysOrgMapper;
    private final SysDataScopeService sysDataScopeService;

    public SysPositionServiceImpl(SysPositionRoleMapper sysPositionRoleMapper,
                                  SysUserPositionMapper sysUserPositionMapper,
                                  SysOrgMapper sysOrgMapper,
                                  SysDataScopeService sysDataScopeService) {
        this.sysPositionRoleMapper = sysPositionRoleMapper;
        this.sysUserPositionMapper = sysUserPositionMapper;
        this.sysOrgMapper = sysOrgMapper;
        this.sysDataScopeService = sysDataScopeService;
    }

    @Override
    public List<PositionDTO> getPositionList(Integer orgId) {
        LambdaQueryWrapper<SysPosition> wrapper = new LambdaQueryWrapper<>();
        if (orgId != null) {
            wrapper.eq(SysPosition::getOrgId, orgId);
        }
        wrapper.orderByAsc(SysPosition::getSortOrder).orderByAsc(SysPosition::getId);
        List<SysPosition> positions = this.list(wrapper);
        return positions.stream().map(p -> {
            PositionDTO dto = new PositionDTO();
            BeanUtils.copyProperties(p, dto);
            SysOrg org = sysOrgMapper.selectById(p.getOrgId());
            if (org != null) {
                dto.setOrgName(org.getOrgName());
            }
            dto.setRoleIds(sysPositionRoleMapper.selectRoleIdsByPositionId(p.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<PositionDTO> getPositionPage(PositionDTO query) {
        Page<SysPosition> page = new Page<>(
                query.getPageNum() != null ? query.getPageNum() : 1,
                query.getPageSize() != null ? query.getPageSize() : 10);
        LambdaQueryWrapper<SysPosition> wrapper = new LambdaQueryWrapper<>();
        if (query.getName() != null && !query.getName().isEmpty()) {
            wrapper.and(w -> w.like(SysPosition::getPositionName, query.getName()));
        }
        if (query.getOrgId() != null) {
            wrapper.and(w -> w.eq(SysPosition::getOrgId, query.getOrgId()));
        }
        wrapper.orderByAsc(SysPosition::getSortOrder).orderByAsc(SysPosition::getId);
        Page<SysPosition> result = this.page(page, wrapper);

        List<PositionDTO> dtoList = result.getRecords().stream().map(p -> {
            PositionDTO dto = new PositionDTO();
            BeanUtils.copyProperties(p, dto);
            dto.setId(p.getId());
            dto.setName(p.getPositionName());
            dto.setCode(p.getPositionCode());
            SysOrg org = sysOrgMapper.selectById(p.getOrgId());
            if (org != null) {
                dto.setOrgName(org.getOrgName());
            }
            return dto;
        }).collect(Collectors.toList());
        return PageResult.of(dtoList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public SysPosition createPosition(PositionDTO dto) {
        String positionCode = dto.getCode() != null ? dto.getCode() : dto.getPositionCode();
        LambdaQueryWrapper<SysPosition> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(SysPosition::getPositionCode, positionCode);
        if (this.count(codeWrapper) > 0) {
            throw new BusinessException(ResultCode.POSITION_CODE_EXISTS);
        }
        SysPosition position = new SysPosition();
        BeanUtils.copyProperties(dto, position, "roleIds", "scopeType", "customOrgIds");
        if (dto.getName() != null) {
            position.setPositionName(dto.getName());
        }
        if (dto.getCode() != null) {
            position.setPositionCode(dto.getCode());
        }
        if (position.getSortOrder() == null) {
            position.setSortOrder(0);
        }
        if (position.getStatus() == null) {
            position.setStatus(1);
        }
        position.setCreatedAt(LocalDateTime.now());
        position.setUpdatedAt(LocalDateTime.now());
        this.save(position);

        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            assignRoles(position.getId(), dto.getRoleIds());
        }
        if (dto.getScopeType() != null) {
            DataScopeDTO dsDto = new DataScopeDTO();
            dsDto.setPositionId(position.getId());
            dsDto.setScopeType(dto.getScopeType());
            dsDto.setCustomOrgIds(dto.getCustomOrgIds());
            sysDataScopeService.setDataScope(position.getId(), dsDto);
        }
        return position;
    }

    @Override
    public SysPosition updatePosition(Integer id, PositionDTO dto) {
        SysPosition position = this.getById(id);
        if (position == null) {
            throw new BusinessException(ResultCode.POSITION_NOT_FOUND);
        }
        String newCode = dto.getCode() != null ? dto.getCode() : dto.getPositionCode();
        if (newCode != null && !newCode.equals(position.getPositionCode())) {
            LambdaQueryWrapper<SysPosition> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(SysPosition::getPositionCode, newCode).ne(SysPosition::getId, id);
            if (this.count(codeWrapper) > 0) {
                throw new BusinessException(ResultCode.POSITION_CODE_EXISTS);
            }
        }
        BeanUtils.copyProperties(dto, position, "id", "roleIds", "scopeType", "customOrgIds");
        if (dto.getName() != null) {
            position.setPositionName(dto.getName());
        }
        if (dto.getCode() != null) {
            position.setPositionCode(dto.getCode());
        }
        position.setUpdatedAt(LocalDateTime.now());
        this.updateById(position);
        return position;
    }

    @Override
    @Transactional
    public void deletePosition(Integer id) {
        SysPosition position = this.getById(id);
        if (position == null) {
            throw new BusinessException(ResultCode.POSITION_NOT_FOUND);
        }
        List<Integer> userIds = sysUserPositionMapper.selectUserIdsByPositionId(id);
        if (userIds != null && !userIds.isEmpty()) {
            throw new BusinessException(ResultCode.POSITION_HAS_USERS);
        }
        LambdaQueryWrapper<SysPositionRole> prWrapper = new LambdaQueryWrapper<>();
        prWrapper.eq(SysPositionRole::getPositionId, id);
        sysPositionRoleMapper.delete(prWrapper);

        LambdaQueryWrapper<SysUserPosition> upWrapper = new LambdaQueryWrapper<>();
        upWrapper.eq(SysUserPosition::getPositionId, id);
        sysUserPositionMapper.delete(upWrapper);

        this.removeById(id);
    }

    @Override
    @Transactional
    public void assignRoles(Integer id, List<Integer> roleIds) {
        SysPosition position = this.getById(id);
        if (position == null) {
            throw new BusinessException(ResultCode.POSITION_NOT_FOUND);
        }
        LambdaQueryWrapper<SysPositionRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPositionRole::getPositionId, id);
        sysPositionRoleMapper.delete(wrapper);

        if (roleIds != null && !roleIds.isEmpty()) {
            for (Integer roleId : roleIds) {
                SysPositionRole pr = new SysPositionRole();
                pr.setPositionId(id);
                pr.setRoleId(roleId);
                pr.setCreatedAt(LocalDateTime.now());
                sysPositionRoleMapper.insert(pr);
            }
        }
    }

    @Override
    public List<Integer> getRoleIdsByPositionId(Integer id) {
        return sysPositionRoleMapper.selectRoleIdsByPositionId(id);
    }

    @Override
    public void setDataScope(Integer id, DataScopeDTO dto) {
        SysPosition position = this.getById(id);
        if (position == null) {
            throw new BusinessException(ResultCode.POSITION_NOT_FOUND);
        }
        dto.setPositionId(id);
        sysDataScopeService.setDataScope(id, dto);
    }

    @Override
    public DataScopeDTO getDataScope(Integer id) {
        return sysDataScopeService.getDataScope(id);
    }
}
