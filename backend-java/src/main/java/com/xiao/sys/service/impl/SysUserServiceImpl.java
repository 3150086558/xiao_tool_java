package com.xiao.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiao.sys.common.BusinessException;
import com.xiao.sys.common.ResultCode;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.dto.UserDTO;
import com.xiao.sys.entity.SysOrg;
import com.xiao.sys.entity.SysUser;
import com.xiao.sys.entity.SysUserPosition;
import com.xiao.sys.mapper.SysOrgMapper;
import com.xiao.sys.mapper.SysUserMapper;
import com.xiao.sys.mapper.SysUserPositionMapper;
import com.xiao.sys.service.SysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserPositionMapper sysUserPositionMapper;
    private final SysOrgMapper sysOrgMapper;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "Xiao@123456";

    public SysUserServiceImpl(SysUserMapper sysUserMapper,
                              SysUserPositionMapper sysUserPositionMapper,
                              SysOrgMapper sysOrgMapper,
                              PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserPositionMapper = sysUserPositionMapper;
        this.sysOrgMapper = sysOrgMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<UserDTO> getUserPage(UserDTO query) {
        int pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();

        IPage<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getUsername, query.getKeyword())
                    .or().like(SysUser::getRealName, query.getKeyword())
                    .or().like(SysUser::getPhone, query.getKeyword())
                    .or().like(SysUser::getEmail, query.getKeyword()));
        }
        if (query.getOrgId() != null) {
            wrapper.eq(SysUser::getOrgId, query.getOrgId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysUser::getId);
        IPage<SysUser> resultPage = this.page(page, wrapper);

        List<UserDTO> dtoList = resultPage.getRecords().stream().map(u -> convertToDTO(u)).collect(Collectors.toList());
        return PageResult.of(dtoList, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    @Transactional
    public SysUser createUser(UserDTO dto) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        if (this.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user, "positionIds", "primaryPositionId", "orgName", "keyword", "pageNum", "pageSize");
        String rawPwd = dto.getPassword() != null && !dto.getPassword().isEmpty() ? dto.getPassword() : DEFAULT_PASSWORD;
        user.setPassword(passwordEncoder.encode(rawPwd));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        this.save(user);

        if (dto.getPositionIds() != null && !dto.getPositionIds().isEmpty()) {
            assignPositions(user.getId(), dto.getPositionIds(), dto.getPrimaryPositionId());
        }
        return user;
    }

    @Override
    @Transactional
    public SysUser updateUser(Integer id, UserDTO dto) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUsername, dto.getUsername()).ne(SysUser::getId, id);
            if (this.count(wrapper) > 0) {
                throw new BusinessException(ResultCode.USERNAME_EXISTS);
            }
        }
        BeanUtils.copyProperties(dto, user, "id", "password", "positionIds", "primaryPositionId", "orgName", "keyword", "pageNum", "pageSize");
        user.setUpdatedAt(LocalDateTime.now());
        this.updateById(user);
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(Integer id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        LambdaQueryWrapper<SysUserPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserPosition::getUserId, id);
        sysUserPositionMapper.delete(wrapper);
        this.removeById(id);
    }

    @Override
    @Transactional
    public void assignPositions(Integer id, List<Integer> positionIds, Integer primaryPositionId) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        LambdaQueryWrapper<SysUserPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserPosition::getUserId, id);
        sysUserPositionMapper.delete(wrapper);

        if (positionIds != null && !positionIds.isEmpty()) {
            for (Integer pid : positionIds) {
                SysUserPosition up = new SysUserPosition();
                up.setUserId(id);
                up.setPositionId(pid);
                up.setIsPrimary(primaryPositionId != null && primaryPositionId.equals(pid));
                up.setCreatedAt(LocalDateTime.now());
                sysUserPositionMapper.insert(up);
            }
        }
    }

    @Override
    public void resetPassword(Integer id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setUpdatedAt(LocalDateTime.now());
        this.updateById(user);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        this.updateById(user);
    }

    @Override
    public List<Integer> getPositionIdsByUserId(Integer userId) {
        return sysUserPositionMapper.selectPositionIdsByUserId(userId);
    }

    private UserDTO convertToDTO(SysUser user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto, "password");
        if (user.getOrgId() != null) {
            SysOrg org = sysOrgMapper.selectById(user.getOrgId());
            if (org != null) {
                dto.setOrgName(org.getOrgName());
            }
        }
        dto.setPositionIds(sysUserPositionMapper.selectPositionIdsByUserId(user.getId()));
        LambdaQueryWrapper<SysUserPosition> pw = new LambdaQueryWrapper<>();
        pw.eq(SysUserPosition::getUserId, user.getId()).eq(SysUserPosition::getIsPrimary, true);
        SysUserPosition primary = sysUserPositionMapper.selectOne(pw);
        if (primary != null) {
            dto.setPrimaryPositionId(primary.getPositionId());
        }
        return dto;
    }
}
