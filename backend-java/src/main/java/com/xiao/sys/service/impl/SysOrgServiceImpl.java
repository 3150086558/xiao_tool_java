package com.xiao.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiao.sys.common.BusinessException;
import com.xiao.sys.common.ResultCode;
import com.xiao.sys.dto.OrgDTO;
import com.xiao.sys.entity.SysOrg;
import com.xiao.sys.entity.SysUser;
import com.xiao.sys.mapper.SysOrgMapper;
import com.xiao.sys.mapper.SysUserMapper;
import com.xiao.sys.service.SysOrgService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysOrgServiceImpl extends ServiceImpl<SysOrgMapper, SysOrg> implements SysOrgService {

    private final SysUserMapper sysUserMapper;

    public SysOrgServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public List<OrgDTO> getOrgTree() {
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysOrg::getSortOrder).orderByAsc(SysOrg::getId);
        List<SysOrg> allOrgs = this.list(wrapper);
        return buildTree(allOrgs);
    }

    @Override
    public SysOrg createOrg(OrgDTO dto) {
        String code = dto.getCode() != null && !dto.getCode().isEmpty() ? dto.getCode() : dto.getOrgCode();
        if (code == null || code.isEmpty()) {
            throw new BusinessException("组织编码不能为空");
        }
        LambdaQueryWrapper<SysOrg> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(SysOrg::getOrgCode, code);
        long count = this.count(codeWrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.ORG_CODE_EXISTS);
        }
        SysOrg org = new SysOrg();
        BeanUtils.copyProperties(dto, org, "name", "code", "sort", "createTime", "parentName");
        org.setOrgName(dto.getName() != null && !dto.getName().isEmpty() ? dto.getName() : dto.getOrgName());
        org.setOrgCode(code);
        if (dto.getSort() != null) {
            org.setSortOrder(dto.getSort());
        }
        if (org.getParentId() == null) {
            org.setParentId(0);
        }
        if (org.getSortOrder() == null) {
            org.setSortOrder(0);
        }
        if (org.getStatus() == null) {
            org.setStatus(1);
        }
        org.setCreatedAt(LocalDateTime.now());
        org.setUpdatedAt(LocalDateTime.now());
        this.save(org);
        return org;
    }

    @Override
    public SysOrg updateOrg(Integer id, OrgDTO dto) {
        SysOrg org = this.getById(id);
        if (org == null) {
            throw new BusinessException(ResultCode.ORG_NOT_FOUND);
        }
        String code = dto.getCode() != null && !dto.getCode().isEmpty() ? dto.getCode() : dto.getOrgCode();
        if (code != null && !code.equals(org.getOrgCode())) {
            LambdaQueryWrapper<SysOrg> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(SysOrg::getOrgCode, code).ne(SysOrg::getId, id);
            if (this.count(codeWrapper) > 0) {
                throw new BusinessException(ResultCode.ORG_CODE_EXISTS);
            }
        }
        if (id.equals(dto.getParentId())) {
            throw new BusinessException("父组织不能为自己");
        }
        BeanUtils.copyProperties(dto, org, "id", "name", "code", "sort", "createTime", "parentName");
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            org.setOrgName(dto.getName());
        }
        if (code != null) {
            org.setOrgCode(code);
        }
        if (dto.getSort() != null) {
            org.setSortOrder(dto.getSort());
        }
        org.setUpdatedAt(LocalDateTime.now());
        this.updateById(org);
        return org;
    }

    @Override
    public void deleteOrg(Integer id) {
        SysOrg org = this.getById(id);
        if (org == null) {
            throw new BusinessException(ResultCode.ORG_NOT_FOUND);
        }
        LambdaQueryWrapper<SysOrg> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(SysOrg::getParentId, id);
        if (this.count(childWrapper) > 0) {
            throw new BusinessException(ResultCode.ORG_HAS_CHILDREN);
        }
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getOrgId, id);
        if (sysUserMapper.selectCount(userWrapper) > 0) {
            throw new BusinessException(ResultCode.ORG_HAS_USERS);
        }
        this.removeById(id);
    }

    @Override
    public List<SysUser> getUsersByOrgId(Integer orgId) {
        return sysUserMapper.selectUsersByOrgId(orgId);
    }

    @Override
    public List<OrgDTO> buildTree(List<SysOrg> orgs) {
        Map<Integer, String> nameMap = orgs.stream()
                .collect(Collectors.toMap(SysOrg::getId, o -> o.getOrgName() != null ? o.getOrgName() : ""));

        List<OrgDTO> nodes = orgs.stream().map(o -> {
            OrgDTO dto = new OrgDTO();
            BeanUtils.copyProperties(o, dto, "orgName", "orgCode", "sortOrder", "createdAt");
            dto.setName(o.getOrgName());
            dto.setCode(o.getOrgCode());
            dto.setSort(o.getSortOrder());
            dto.setCreateTime(o.getCreatedAt() != null ? o.getCreatedAt().toString().replace("T", " ") : null);
            return dto;
        }).collect(Collectors.toList());

        Map<Integer, OrgDTO> nodeMap = nodes.stream()
                .collect(Collectors.toMap(OrgDTO::getId, n -> n));

        List<OrgDTO> roots = new ArrayList<>();
        for (OrgDTO node : nodes) {
            Integer parentId = node.getParentId();
            if (parentId != null && parentId != 0) {
                node.setParentName(nameMap.getOrDefault(parentId, ""));
            }
            if (parentId == null || parentId == 0) {
                roots.add(node);
            } else {
                OrgDTO parent = nodeMap.get(parentId);
                if (parent != null) {
                    parent.addChild(node);
                } else {
                    roots.add(node);
                }
            }
        }
        return roots;
    }

    @Override
    public List<Integer> getChildOrgIds(Integer parentId) {
        List<Integer> result = new ArrayList<>();
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrg::getParentId, parentId);
        List<SysOrg> children = this.list(wrapper);
        for (SysOrg child : children) {
            result.add(child.getId());
            result.addAll(getChildOrgIds(child.getId()));
        }
        return result;
    }
}
