package com.xiao.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiao.sys.common.BusinessException;
import com.xiao.sys.common.ResultCode;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.dto.RoleDTO;
import com.xiao.sys.entity.SysRole;
import com.xiao.sys.entity.SysRoleMenu;
import com.xiao.sys.mapper.SysRoleMapper;
import com.xiao.sys.mapper.SysRoleMenuMapper;
import com.xiao.sys.service.SysRoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMenuMapper sysRoleMenuMapper;

    public SysRoleServiceImpl(SysRoleMenuMapper sysRoleMenuMapper) {
        this.sysRoleMenuMapper = sysRoleMenuMapper;
    }

    @Override
    public List<SysRole> getRoleList() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysRole::getId);
        return this.list(wrapper);
    }

    @Override
    public PageResult<RoleDTO> getRolePage(RoleDTO query) {
        Page<SysRole> page = new Page<>(
                query.getPageNum() != null ? query.getPageNum() : 1,
                query.getPageSize() != null ? query.getPageSize() : 10);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (query.getName() != null && !query.getName().isEmpty()) {
            wrapper.and(w -> w.like(SysRole::getRoleName, query.getName()));
        }
        if (query.getCode() != null && !query.getCode().isEmpty()) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, query.getCode()));
        }
        wrapper.orderByAsc(SysRole::getId);
        Page<SysRole> result = this.page(page, wrapper);

        List<RoleDTO> dtoList = result.getRecords().stream().map(r -> {
            RoleDTO dto = new RoleDTO();
            BeanUtils.copyProperties(r, dto);
            dto.setName(r.getRoleName());
            dto.setCode(r.getRoleCode());
            return dto;
        }).collect(Collectors.toList());
        return PageResult.of(dtoList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public SysRole createRole(RoleDTO dto) {
        String roleCode = dto.getCode() != null ? dto.getCode() : dto.getRoleCode();
        LambdaQueryWrapper<SysRole> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(SysRole::getRoleCode, roleCode);
        if (this.count(codeWrapper) > 0) {
            throw new BusinessException(ResultCode.ROLE_CODE_EXISTS);
        }
        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role, "menuIds");
        if (dto.getName() != null) {
            role.setRoleName(dto.getName());
        }
        if (dto.getCode() != null) {
            role.setRoleCode(dto.getCode());
        }
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        this.save(role);
        if (dto.getMenuIds() != null && !dto.getMenuIds().isEmpty()) {
            assignMenus(role.getId(), dto.getMenuIds());
        }
        return role;
    }

    @Override
    public SysRole updateRole(Integer id, RoleDTO dto) {
        SysRole role = this.getById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }
        String newCode = dto.getCode() != null ? dto.getCode() : dto.getRoleCode();
        if (newCode != null && !newCode.equals(role.getRoleCode())) {
            LambdaQueryWrapper<SysRole> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(SysRole::getRoleCode, newCode).ne(SysRole::getId, id);
            if (this.count(codeWrapper) > 0) {
                throw new BusinessException(ResultCode.ROLE_CODE_EXISTS);
            }
        }
        if (dto.getName() != null) {
            role.setRoleName(dto.getName());
        }
        if (dto.getCode() != null) {
            role.setRoleCode(dto.getCode());
        }
        if (dto.getStatus() != null) {
            role.setStatus(dto.getStatus());
        }
        if (dto.getDescription() != null) {
            role.setDescription(dto.getDescription());
        }
        role.setUpdatedAt(LocalDateTime.now());
        this.updateById(role);
        return role;
    }

    @Override
    @Transactional
    public void deleteRole(Integer id) {
        SysRole role = this.getById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, id);
        sysRoleMenuMapper.delete(wrapper);
        this.removeById(id);
    }

    @Override
    @Transactional
    public void assignMenus(Integer id, List<Integer> menuIds) {
        SysRole role = this.getById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, id);
        sysRoleMenuMapper.delete(wrapper);

        if (menuIds != null && !menuIds.isEmpty()) {
            for (Integer menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(id);
                rm.setMenuId(menuId);
                rm.setCreatedAt(LocalDateTime.now());
                sysRoleMenuMapper.insert(rm);
            }
        }
    }

    @Override
    public List<Integer> getMenuIdsByRoleId(Integer id) {
        return sysRoleMenuMapper.selectMenuIdsByRoleId(id);
    }
}
