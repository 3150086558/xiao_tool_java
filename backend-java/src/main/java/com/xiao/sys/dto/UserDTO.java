package com.xiao.sys.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO {

    private Integer id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private Integer orgId;
    private String orgName;
    private Integer status;
    private List<Integer> positionIds;
    private Integer primaryPositionId;
    private LocalDateTime createdAt;

    // 查询用字段
    private String keyword;
    private Integer pageNum = 1;
    private Integer pageSize = 20;
}
