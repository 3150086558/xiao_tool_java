package com.xiao.sys.dto;

import java.time.LocalDateTime;
import java.util.List;

public class UserDTO {

    private Integer id;
    private String username;
    private String password;
    private String name;
    private String realName;
    private String email;
    private String phone;
    private Integer orgId;
    private String orgName;
    private Integer status;
    private List<Integer> positionIds;
    private Integer primaryPositionId;
    private LocalDateTime createdAt;
    private String createTime;

    private String keyword;
    private Integer pageNum = 1;
    private Integer pageSize = 20;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getOrgId() { return orgId; }
    public void setOrgId(Integer orgId) { this.orgId = orgId; }

    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public List<Integer> getPositionIds() { return positionIds; }
    public void setPositionIds(List<Integer> positionIds) { this.positionIds = positionIds; }

    public Integer getPrimaryPositionId() { return primaryPositionId; }
    public void setPrimaryPositionId(Integer primaryPositionId) { this.primaryPositionId = primaryPositionId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
