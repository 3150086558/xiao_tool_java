package com.xiao.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiao.sys.common.BusinessException;
import com.xiao.sys.common.ResultCode;
import com.xiao.sys.dto.MenuDTO;
import com.xiao.sys.dto.MenuTreeNode;
import com.xiao.sys.entity.SysMenu;
import com.xiao.sys.mapper.SysMenuMapper;
import com.xiao.sys.mapper.SysPositionRoleMapper;
import com.xiao.sys.mapper.SysUserPositionMapper;
import com.xiao.sys.service.SysMenuService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysUserPositionMapper sysUserPositionMapper;
    private final SysPositionRoleMapper sysPositionRoleMapper;

    public SysMenuServiceImpl(SysUserPositionMapper sysUserPositionMapper,
                              SysPositionRoleMapper sysPositionRoleMapper) {
        this.sysUserPositionMapper = sysUserPositionMapper;
        this.sysPositionRoleMapper = sysPositionRoleMapper;
    }

    @Override
    public List<MenuTreeNode> getMenuTree() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysMenu::getSortOrder).orderByAsc(SysMenu::getId);
        List<SysMenu> allMenus = this.list(wrapper);
        return buildTree(allMenus);
    }

    @Override
    public List<MenuTreeNode> getUserMenuTree(Integer userId) {
        List<Integer> positionIds = sysUserPositionMapper.selectPositionIdsByUserId(userId);
        if (positionIds == null || positionIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> roleIds = sysPositionRoleMapper.selectRoleIdsByPositionIds(positionIds);
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysMenu> menus = baseMapper.selectMenusByRoleIds(roleIds);
        return buildTree(menus);
    }

    @Override
    public SysMenu createMenu(MenuDTO dto) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu, "title", "type");
        if (dto.getTitle() != null && !dto.getTitle().isEmpty()) {
            menu.setMenuName(dto.getTitle());
        }
        if (dto.getType() != null) {
            menu.setMenuType(mapTypeToCode(dto.getType()));
        }
        if (menu.getParentId() == null) {
            menu.setParentId(0);
        }
        if (menu.getVisible() == null) {
            menu.setVisible(1);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        if (menu.getSortOrder() == null) {
            menu.setSortOrder(0);
        }
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        this.save(menu);
        return menu;
    }

    @Override
    public SysMenu updateMenu(Integer id, MenuDTO dto) {
        SysMenu menu = this.getById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.MENU_NOT_FOUND);
        }
        BeanUtils.copyProperties(dto, menu, "id", "title", "type");
        if (dto.getTitle() != null && !dto.getTitle().isEmpty()) {
            menu.setMenuName(dto.getTitle());
        }
        if (dto.getType() != null) {
            menu.setMenuType(mapTypeToCode(dto.getType()));
        }
        menu.setUpdatedAt(LocalDateTime.now());
        this.updateById(menu);
        return menu;
    }

    @Override
    public void deleteMenu(Integer id) {
        SysMenu menu = this.getById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.MENU_NOT_FOUND);
        }
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, id);
        long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.MENU_HAS_CHILDREN);
        }
        this.removeById(id);
    }

    @Override
    public List<MenuTreeNode> buildTree(List<SysMenu> menus) {
        List<MenuTreeNode> nodes = menus.stream().map(m -> {
            MenuTreeNode node = new MenuTreeNode();
            BeanUtils.copyProperties(m, node);
            node.setTitle(m.getMenuName());
            node.setType(mapTypeToInt(m.getMenuType()));
            return node;
        }).collect(Collectors.toList());

        Map<Integer, MenuTreeNode> nodeMap = nodes.stream()
                .collect(Collectors.toMap(MenuTreeNode::getId, n -> n));

        List<MenuTreeNode> roots = new ArrayList<>();
        for (MenuTreeNode node : nodes) {
            Integer parentId = node.getParentId();
            if (parentId == null || parentId == 0) {
                roots.add(node);
            } else {
                MenuTreeNode parent = nodeMap.get(parentId);
                if (parent != null) {
                    parent.addChild(node);
                } else {
                    roots.add(node);
                }
            }
        }
        return roots;
    }

    private static String mapTypeToCode(Integer type) {
        if (type == null) return "M";
        return switch (type) {
            case 1 -> "D";
            case 3 -> "B";
            default -> "M";
        };
    }

    private static Integer mapTypeToInt(String menuType) {
        if (menuType == null) return 2;
        return switch (menuType) {
            case "D" -> 1;
            case "B" -> 3;
            default -> 2;
        };
    }
}
