package com.xiao.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiao.sys.dto.MenuDTO;
import com.xiao.sys.dto.MenuTreeNode;
import com.xiao.sys.entity.SysMenu;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    List<MenuTreeNode> getMenuTree();

    List<MenuTreeNode> getUserMenuTree(Integer userId);

    SysMenu createMenu(MenuDTO dto);

    SysMenu updateMenu(Integer id, MenuDTO dto);

    void deleteMenu(Integer id);

    List<MenuTreeNode> buildTree(List<SysMenu> menus);
}
