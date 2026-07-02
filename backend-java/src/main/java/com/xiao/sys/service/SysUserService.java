package com.xiao.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiao.sys.dto.PageResult;
import com.xiao.sys.dto.UserDTO;
import com.xiao.sys.entity.SysUser;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    PageResult<UserDTO> getUserPage(UserDTO query);

    SysUser createUser(UserDTO dto);

    SysUser updateUser(Integer id, UserDTO dto);

    void deleteUser(Integer id);

    void assignPositions(Integer id, List<Integer> positionIds, Integer primaryPositionId);

    void resetPassword(Integer id);

    void resetPassword(Integer id, String password);

    void updateStatus(Integer id, Integer status);

    List<Integer> getPositionIdsByUserId(Integer userId);
}
