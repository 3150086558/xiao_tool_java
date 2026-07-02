package com.xiao.sys.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrgDTO {

    private Integer id;
    private Integer parentId;
    private String name;
    private String code;
    private String orgName;
    private String orgCode;
    private Integer sort;
    private Integer sortOrder;
    private Integer status;
    private Integer leaderId;
    private String leader;
    private String phone;
    private String remark;
    private String parentName;
    private String createTime;
    private List<OrgDTO> children = new ArrayList<>();

    public void addChild(OrgDTO child) {
        this.children.add(child);
    }
}
