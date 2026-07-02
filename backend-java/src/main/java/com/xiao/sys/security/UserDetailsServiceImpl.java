package com.xiao.sys.security;

import com.xiao.sys.entity.SysUser;
import com.xiao.sys.entity.SysMenu;
import com.xiao.sys.mapper.SysMenuMapper;
import com.xiao.sys.mapper.SysPositionRoleMapper;
import com.xiao.sys.mapper.SysUserMapper;
import com.xiao.sys.mapper.SysUserPositionMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysUserPositionMapper sysUserPositionMapper;
    private final SysPositionRoleMapper sysPositionRoleMapper;
    private final SysMenuMapper sysMenuMapper;

    public UserDetailsServiceImpl(SysUserMapper sysUserMapper,
                                  SysUserPositionMapper sysUserPositionMapper,
                                  SysPositionRoleMapper sysPositionRoleMapper,
                                  SysMenuMapper sysMenuMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserPositionMapper = sysUserPositionMapper;
        this.sysPositionRoleMapper = sysPositionRoleMapper;
        this.sysMenuMapper = sysMenuMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new UsernameNotFoundException("账号已被禁用: " + username);
        }

        List<String> permissions = loadPermissions(user.getId());
        return new LoginUser(user, permissions);
    }

    public List<String> loadPermissions(Integer userId) {
        List<Integer> positionIds = sysUserPositionMapper.selectPositionIdsByUserId(userId);
        if (positionIds == null || positionIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> roleIds = sysPositionRoleMapper.selectRoleIdsByPositionIds(positionIds);
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<SysMenu> menus = sysMenuMapper.selectMenusByRoleIds(roleIds);
        return menus.stream()
                .map(SysMenu::getPermission)
                .filter(p -> p != null && !p.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    public SysUser loadUserById(Integer userId) {
        return sysUserMapper.selectById(userId);
    }
}
