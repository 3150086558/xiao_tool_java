package com.xiao.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.sys.common.BusinessException;
import com.xiao.sys.common.ResultCode;
import com.xiao.sys.dto.ChangePasswordDTO;
import com.xiao.sys.dto.LoginDTO;
import com.xiao.sys.dto.MenuTreeNode;
import com.xiao.sys.dto.UserInfoDTO;
import com.xiao.sys.entity.SysOrg;
import com.xiao.sys.entity.SysPositionDataScope;
import com.xiao.sys.entity.SysRole;
import com.xiao.sys.entity.SysUser;
import com.xiao.sys.mapper.SysOrgMapper;
import com.xiao.sys.mapper.SysPositionDataScopeMapper;
import com.xiao.sys.mapper.SysPositionRoleMapper;
import com.xiao.sys.mapper.SysRoleMapper;
import com.xiao.sys.mapper.SysUserMapper;
import com.xiao.sys.mapper.SysUserPositionMapper;
import com.xiao.sys.security.JwtTokenProvider;
import com.xiao.sys.security.LoginUser;
import com.xiao.sys.security.UserDetailsServiceImpl;
import com.xiao.sys.service.AuthService;
import com.xiao.sys.service.SysDataScopeService;
import com.xiao.sys.service.SysMenuService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysUserPositionMapper sysUserPositionMapper;
    private final SysPositionRoleMapper sysPositionRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysOrgMapper sysOrgMapper;
    private final SysPositionDataScopeMapper sysPositionDataScopeMapper;
    private final SysMenuService sysMenuService;
    private final SysDataScopeService sysDataScopeService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthServiceImpl(SysUserMapper sysUserMapper,
                           SysUserPositionMapper sysUserPositionMapper,
                           SysPositionRoleMapper sysPositionRoleMapper,
                           SysRoleMapper sysRoleMapper,
                           SysOrgMapper sysOrgMapper,
                           SysPositionDataScopeMapper sysPositionDataScopeMapper,
                           SysMenuService sysMenuService,
                           SysDataScopeService sysDataScopeService,
                           JwtTokenProvider jwtTokenProvider,
                           PasswordEncoder passwordEncoder,
                           UserDetailsServiceImpl userDetailsService) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserPositionMapper = sysUserPositionMapper;
        this.sysPositionRoleMapper = sysPositionRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysOrgMapper = sysOrgMapper;
        this.sysPositionDataScopeMapper = sysPositionDataScopeMapper;
        this.sysMenuService = sysMenuService;
        this.sysDataScopeService = sysDataScopeService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        SysUser user = sysUserMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, dto.getUsername()));
        }
        if (user == null) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        result.put("userInfo", userInfo);
        result.put("expiresIn", 604800000L);
        return result;
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    public UserInfoDTO getCurrentUserInfo() {
        LoginUser loginUser = getLoginUser();
        SysUser user = loginUser.getUser();

        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setOrgId(user.getOrgId());
        dto.setStatus(user.getStatus());

        if (user.getOrgId() != null) {
            SysOrg org = sysOrgMapper.selectById(user.getOrgId());
            if (org != null) {
                dto.setOrgName(org.getOrgName());
            }
        }

        List<Integer> positionIds = sysUserPositionMapper.selectPositionIdsByUserId(user.getId());
        dto.setPositionIds(positionIds);

        List<String> permissions = userDetailsService.loadPermissions(user.getId());
        dto.setPermissions(permissions);

        List<Integer> roleIds = positionIds == null || positionIds.isEmpty()
                ? new ArrayList<>()
                : sysPositionRoleMapper.selectRoleIdsByPositionIds(positionIds);
        List<String> roleCodes = new ArrayList<>();
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
            roleCodes = roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
        }
        dto.setRoles(roleCodes);

        List<MenuTreeNode> menuTree = sysMenuService.getUserMenuTree(user.getId());
        dto.setMenus(menuTree);

        List<Map<String, Object>> dataScopes = new ArrayList<>();
        if (positionIds != null && !positionIds.isEmpty()) {
            List<SysPositionDataScope> scopes = sysPositionDataScopeMapper.selectByPositionIds(positionIds);
            if (scopes != null) {
                for (SysPositionDataScope scope : scopes) {
                    Map<String, Object> ds = new HashMap<>();
                    ds.put("positionId", scope.getPositionId());
                    ds.put("scopeType", scope.getScopeType());
                    dataScopes.add(ds);
                }
            }
        }
        dto.setDataScopes(dataScopes);

        List<Integer> visibleOrgIds = sysDataScopeService.getVisibleOrgIds(user.getId());
        dto.setVisibleOrgIds(visibleOrgIds);

        return dto;
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        LoginUser loginUser = getLoginUser();
        SysUser user = loginUser.getUser();

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        update.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(update);
    }

    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return (LoginUser) authentication.getPrincipal();
    }
}
